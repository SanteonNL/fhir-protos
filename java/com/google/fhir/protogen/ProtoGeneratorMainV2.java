//    Copyright 2023 Google Inc.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        https://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

package com.google.fhir.protogen;

import static com.google.fhir.protogen.FieldRetagger.retagMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.fhir.common.InvalidFhirException;
import com.google.fhir.proto.ProtogenConfig;
import com.google.fhir.r4.core.StructureDefinition;
import com.google.fhir.r4.core.StructureDefinitionKindCode;
import com.google.fhir.r4.core.TypeDerivationRuleCode;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A class that runs ProtoGenerator on the specified inputs, turning FHIR StructureDefinition files
 * into proto descriptors. Depending on settings, either the descriptors, the .proto file, or both
 * will be emitted.
 */
class ProtoGeneratorMainV2 {

  private final Args args;

  private static final String BUNDLE_STRUCTURE_DEFINITION_URL =
      "http://hl7.org/fhir/StructureDefinition/Bundle";

  private static class Args {
    @Parameter(
        names = {"--output_directory"},
        description =
            "The directory in the source tree that the proto files will be located. "
                + "This allows for intra-package imports, like codes and extensions.")
    private String outputDirectory = null;

    @Parameter(
        names = {"--input_package"},
        description = "Input FHIR package",
        required = true)
    private String inputPackageLocation = null;

    @Parameter(
        names = {"--proto_package"},
        description = "Proto package for generated messages",
        required = true)
    private String protoPackage = null;

    @Parameter(
        names = {"--java_proto_package"},
        description = "Java proto package for generated messages",
        required = true)
    private String javaProtoPackage = null;

    @Parameter(
        names = {"--license_date"},
        description = "Date to use for Apache License",
        required = true)
    private String licenseDate = null;

    @Parameter(
        names = {"--contained_resource_offset"},
        description =
            "Field number offset for ContainedResources.  This is used to ensure that"
                + " ContainedResources from different versions of FHIR don't use overlapping"
                + " numbers, so they can eventually be combined.  See: go/StableFhirProtos")
    private int containedResourceOffset = 0;

    @Parameter(
        names = {"--legacy_retagging"},
        description = "If true, will ensure tag numbers align with R4.")
    private boolean legacyRetagging = false;
  }

  ProtoGeneratorMainV2(Args args) {
    this.args = args;
  }

  private static class ProtoFile {
    final String filepath;
    FileDescriptorProto fileDescriptor;

    ProtoFile(String filepath, FileDescriptorProto fileDescriptor) {
      this.filepath = filepath;
      this.fileDescriptor = fileDescriptor;
    }
  }

  void runSingleVersionMode(FhirPackage inputPackage) throws IOException, InvalidFhirException {
    ProtogenConfig config =
        ProtogenConfig.newBuilder()
            .setProtoPackage(args.protoPackage)
            .setJavaProtoPackage(args.javaProtoPackage)
            .setLicenseDate(args.licenseDate)
            .setSourceDirectory(args.outputDirectory)
            .build();

    List<ProtoFile> files = makePackageFiles(inputPackage, config);
    if (args.legacyRetagging) {
      retagFiles(files);
    }

    try (ZipOutputStream zipOutputStream =
        new ZipOutputStream(new FileOutputStream(new File(args.outputDirectory, "output.zip")))) {
      addFilesToZip(zipOutputStream, new ProtoFilePrinter(config), files);
    }
  }

  private List<ProtoFile> makePackageFiles(FhirPackage fhirPackage, ProtogenConfig config)
      throws InvalidFhirException {
    List<ProtoFile> files = new ArrayList<>();

    // Add Terminology files.  These are generated by the ValueSetGenerator using the package and
    // config passed during construction.
    ValueSetGeneratorV2 valueSetGenerator = new ValueSetGeneratorV2(fhirPackage, config);
    // Contains all non-trivial ValueSets used by the FhirPackage for bindings (i.e., the
    // FhirPackage contains a code bound to that ValueSet.
    // A ValueSet is considered trivial if its contents are 1-1 with a CodeSystem.
    files.add(new ProtoFile("valuesets.proto", valueSetGenerator.makeValueSetFile()));
    // All CodeSystems corresponding to trivial ValueSets from the above step.
    // We break these down this way because the spec contains many duplicate ValueSets that
    // are trivially identical to a single CodeSystem.  Making Codes that are bound to these
    // ValueSets use a common CodeSystem definition results in many fewer redundant ValueSets.
    files.add(new ProtoFile("codes.proto", valueSetGenerator.makeCodeSystemFile()));

    ProtoGeneratorV2 generator = new ProtoGeneratorV2(fhirPackage, config);

    List<String> resourceNames = new ArrayList<>();
    StructureDefinition bundleDefinition = null;
    String semanticVersion = fhirPackage.getSemanticVersion();
    // Iterate over all non-bundle Resources, and generate a single file per resource.
    // Aggregate resource names for use in generating a "Bundle and ContainedResource" file,
    // as well as generating a typed reference datatype.
    for (StructureDefinition structDef : fhirPackage.structureDefinitions()) {
      if (structDef.getKind().getValue() == StructureDefinitionKindCode.Value.RESOURCE
          && structDef.getDerivation().getValue() == TypeDerivationRuleCode.Value.SPECIALIZATION
          && !structDef.getAbstract().getValue()) {
        String resourceName = GeneratorUtils.getTypeName(structDef);
        resourceNames.add(resourceName);
        if (structDef.getUrl().getValue().equals(BUNDLE_STRUCTURE_DEFINITION_URL)) {
          // Don't make a bundle resource - it will be generated with the ContainedResource.
          if (bundleDefinition != null) {
            throw new InvalidFhirException("More than one bundle resource found");
          }
          bundleDefinition = structDef;
        } else {
          files.add(
              new ProtoFile(
                  "resources/" + GeneratorUtils.resourceNameToFileName(resourceName),
                  generator.generateResourceFileDescriptor(structDef, semanticVersion)));
        }
      }
    }

    // Generate the "Bundle and Contained Resource" file.
    files.add(
        new ProtoFile(
            "resources/bundle_and_contained_resource.proto",
            generator.generateBundleAndContainedResource(
                bundleDefinition, semanticVersion, resourceNames, args.containedResourceOffset)));

    if (fhirPackage.getSemanticVersion().equals("4.0.1")) {
      // Old R4 had a few reference types to non-concrete resources.  Include these to be
      // backwards
      // compatible during transition.
      // TODO(b/299644315): Consider dropping these fields and reserving the field numbers
      // instead.
      resourceNames.add("DomainResource");
      resourceNames.add("MetadataResource");
    }
    // Generate the Datatypes file.  Pass all resource names, for use in generating the
    // Reference datatype.
    files.add(
        new ProtoFile("datatypes.proto", generator.generateDatatypesFileDescriptor(resourceNames)));

    // Set the Go Package.
    // Note that Go package is set outside of the generator, since it needs to know the filepath.
    files.forEach(
        file ->
            file.fileDescriptor =
                GeneratorUtils.setGoPackage(
                    file.fileDescriptor, config.getSourceDirectory(), file.filepath));
    return files;
  }

  private static void addFilesToZip(
      ZipOutputStream zipOutputStream, ProtoFilePrinter printer, List<ProtoFile> protoFiles)
      throws IOException {
    for (ProtoFile protoFile : protoFiles) {
      zipOutputStream.putNextEntry(new ZipEntry(protoFile.filepath));
      byte[] entryBytes = printer.print(protoFile.fileDescriptor).getBytes(UTF_8);
      zipOutputStream.write(entryBytes, 0, entryBytes.length);
    }
  }

  // Retags a list of proto files against their current R4 counterparts, where possible.
  // This process ensures that any field that is the same in the reference package (current R4) as
  // the package being generated will have the same tag numbers, and any new fields in the
  // new message will use a tag number that is not in use by the reference counterpart.
  //
  // This is done to grant the maximum possible flexibility for ultimately moving to a combined
  // versionless representation of normative resources.  For instance, since Patient is normative,
  // an R4 or an R5 Patient should theoretically "fit" in an R6 proto, but this is only possible if
  // tag numbers line up between versions.  This currently uses R4 as the reference version, but
  // ultimately this should use the most recent published version.
  //
  // Note that this is a best-effort algorithm, and does not guarantee binary compatibility.
  // Binary compatibility should be independently verified before anything relies on it.
  private static void retagFiles(List<ProtoFile> files) {
    // Whether we've found ANY matching descriptors.  This serves as a sanity check.
    boolean foundMatchingDescriptor = false;
    for (ProtoFile file : files) {
      List<DescriptorProto> retaggedMessageTypeList = new ArrayList<>();
      for (DescriptorProto descriptor : file.fileDescriptor.getMessageTypeList()) {
        String fullName =
            file.fileDescriptor.getOptions().getJavaPackage() + "." + descriptor.getName();

        // Name of this message in the reference (i.e., current R4) package.
        String r4FullName = fullName.replaceFirst("\\.r[0-9]*\\.", ".r4.");

        // Skip ContainedResource - each version gets its own range of numbers in ContainedResource,
        // governed by the `contained_resource_offset` flag.
        if (fullName.endsWith(".ContainedResource")) {
          retaggedMessageTypeList.add(descriptor);
          continue;
        }

        // Skip SearchParameters and OperationDefinition for now, since there is an issue with
        // changed ValueSets.
        // TODO(b/315841051): Figure out something smart to do here.
        if (fullName.endsWith(".SearchParameter") || fullName.endsWith(".OperationDefinition")) {
          System.out.println("Warning!  Skipping " + fullName);
          retaggedMessageTypeList.add(descriptor);
          continue;
        }

        try {
          Class<?> r4MessageClass = Class.forName(r4FullName);
          try {
            DescriptorProto r4Descriptor =
                ((Descriptor) r4MessageClass.getMethod("getDescriptor").invoke(null)).toProto();
            retaggedMessageTypeList.add(retagMessage(descriptor, r4Descriptor));
            foundMatchingDescriptor = true;
          } catch (ReflectiveOperationException e) {
            // If we find a class with the expected name, it should always have a "getDescriptor".
            throw new IllegalStateException(e);
          }
        } catch (ClassNotFoundException e) {
          // No matching class in R4 - that's ok, it's something new in this version.
          retaggedMessageTypeList.add(descriptor);
        }
        file.fileDescriptor =
            file.fileDescriptor.toBuilder()
                .clearMessageType()
                .addAllMessageType(retaggedMessageTypeList)
                .build();
      }
    }
    if (!foundMatchingDescriptor) {
      // We didn't find a single match.  This almost certainly means that there is a problem with
      // how we're resolving matches - e.g., missing a run-time dep, or inferring wrong class names.
      throw new AssertionError(
          "Legacy field tagging requested, but no matching descriptors found.");
    }
  }

  public static void main(String[] argv) throws IOException, InvalidFhirException {
    // Each non-flag argument is assumed to be an input file.
    Args args = new Args();
    JCommander jcommander = new JCommander(args);
    try {
      jcommander.parse(argv);
    } catch (ParameterException exception) {
      System.err.printf("Invalid usage: %s\n", exception.getMessage());
      System.exit(1);
    }
    new ProtoGeneratorMainV2(args)
        .runSingleVersionMode(
            FhirPackage.load(
                args.inputPackageLocation,
                /* no manually added package info - read from package */ null,
                /* ignoreUnrecognizedFieldsAndCodes= */ true));
  }
}
