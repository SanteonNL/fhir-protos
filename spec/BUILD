# Data files containing the base FHIR specification.

load("//bazel:protogen.bzl", "fhir_package")

package(
    
    default_visibility = ["//visibility:public"],
)

licenses(["notice"])

exports_files(["uscore_package_info.prototxt"])

# Technical corrections for 4.0.1.
filegroup(
    name = "core_4_0_1_technical_corections",
    srcs = ["hl7.fhir.core/4.0.1/technical_corrections.textproto"],
)

# Current DSTU2 package
filegroup(
    name = "dstu2",
    srcs = glob([
        "hl7.fhir.core/1.0.2/package/*.json",
    ]),
)

# Current STU3 package
filegroup(
    name = "stu3",
    srcs = glob([
        "hl7.fhir.core/3.0.1/package/*.json",
    ]),
)

# STU3 core types.
fhir_package(
    name = "fhir_stu3",
    definitions = glob(
        ["hl7.fhir.core/3.0.1/package/StructureDefinition-*.json"],
        exclude = [
            "hl7.fhir.core/3.0.1/package/StructureDefinition-StructureDefinition.json",
            "hl7.fhir.core/3.0.1/package/StructureDefinition-cqif-condition.json",
            "hl7.fhir.core/3.0.1/package/StructureDefinition-cqif-cqlExpression.json",
            "hl7.fhir.core/3.0.1/package/StructureDefinition-cqif-fhirPathExpression.json",
            "hl7.fhir.core/3.0.1/package/StructureDefinition-cqif-library.json",
            "hl7.fhir.core/3.0.1/package/StructureDefinition-familymemberhistory-genetic.json",
        ],
    ) + glob([
        "hl7.fhir.core/3.0.1/modified/StructureDefinition-*.json",
    ]) + glob(
        [
            "hl7.fhir.core/3.0.1/package/ValueSet-*.json",
        ],
    ) + glob(
        [
            "hl7.fhir.core/3.0.1/package/CodeSystem-*.json",
        ],
    ) + ["hl7.fhir.core/3.0.1/package/package.json"],
    package_info = "fhir_stu3_package_info.prototxt",
)

# Current R4 package
filegroup(
    name = "r4",
    srcs = glob([
        "hl7.fhir.core/4.0.1/package/*.json",
    ]),
)

# Example testdata, e.g., Patient-example.json, was dropped in the 4.0.1 npm
filegroup(
    name = "r4_examples",
    srcs = glob([
        "hl7.fhir.r4.examples/4.0.1/package/*",
    ]),
)

fhir_package(
    name = "fhir_r4",
    definitions =
        glob(
            ["hl7.fhir.core/4.0.1/package/StructureDefinition-*.json"],
            exclude = [
                "hl7.fhir.core/4.0.1/package/StructureDefinition-example-*.json",
                "hl7.fhir.core/4.0.1/package/StructureDefinition-*.*.json",
                "hl7.fhir.core/4.0.1/package/StructureDefinition-structuredefinition-xml-type.json",
                "hl7.fhir.core/4.0.1/package/StructureDefinition-structuredefinition-rdf-type.json",
                "hl7.fhir.core/4.0.1/package/StructureDefinition-structuredefinition-json-type.json",
            ],
        ) + glob(
            [
                "hl7.fhir.core/4.0.1/package/ValueSet-*.json",
            ],
        ) + glob(
            ["hl7.fhir.core/4.0.1/package/CodeSystem-*.json"],
        ) + glob(
            ["hl7.fhir.core/4.0.1/package/SearchParameter-*.json"],
            exclude = [
                "hl7.fhir.core/4.0.1/package/SearchParameter-*-extensions-*.json",
            ],
        ) + ["hl7.fhir.core/4.0.1/package/package.json"],
    package_info = "fhir_r4_package_info.prototxt",
)

# US-Core profiles for FHIR STU3.
fhir_package(
    name = "uscore_stu3_package",
    definitions = glob([
        "hl7.fhir.us.core/2.0.0/package/StructureDefinition-*.json",
    ]),
    package_info = "uscore_stu3_package_info.prototxt",
)

# US-Core Profiles for FHIR R4

fhir_package(
    name = "uscore_r4_package",
    definitions = glob([
        "hl7.fhir.us.core/3.1.0/package/StructureDefinition-*.json",
    ]) + glob(["hl7.fhir.us.core/3.1.0/package/ValueSet-*.json"]) + glob([
        "hl7.fhir.us.core/3.1.0/package/CodeSystem-*.json",
    ]) + ["hl7.fhir.us.core/3.1.0/package/package.json"],
    package_info = "uscore_r4_package_info.prototxt",
)

# NL-Core Profiles for FHIR R4
# temp commands
# bazel build //proto/google/fhir/proto/r4:nlcore_proto_zip --verbose_failures

# unzip bazel-bin/proto/google/fhir/proto/r4/nlcore.zip -d proto/google/fhir/proto/r4

# protoc --go_opt=Mproto/google/fhir/proto/r4/nlcore.proto=github.com/santeonnl/fhir-protos/go/proto/google/fhir/proto/r4/nlcore_go_proto --go_out=tmp proto/google/fhir/proto/r4/nlcore.proto

# sudo apt install python3-pip
# pip install -r bazel/requirements.txt


fhir_package(
    name = "nlcore_r4_package",
    definitions = [".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-AddressInformation.json",#
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-NameInformation.json",#
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-HealthProfessional-PractitionerRole.json",
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-HealthProfessional-Practitioner.json",
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-HealthcareProvider-Organization.json",
    ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-HealthcareProvider.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.nl-core#0.10.0-beta.1/package/nl-core-Patient.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/ext-Comment.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-HealthProfessional-Practitioner.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-HealthcareProvider-Organization.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-HealthcareProvider.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-HealthProfessional-PractitionerRole.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-NameInformation.json",#
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/ext-CodeSpecification.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/ext-AddressInformation.AddressType.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-AddressInformation.json",#
    # PatientNationality
    # PatientProficiency
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    # ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/zib-Patient.json",
    ".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/ext-LanguageProficiency.CommunicationDetails.json"]
     + [".fhir/packages/nictiz.fhir.nl.r4.zib2020#0.10.0-beta.1/package/package.json"],
    package_info = "nlcore_r4_package_info.prototxt",
)
