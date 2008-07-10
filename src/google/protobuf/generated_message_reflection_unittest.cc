// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.
// http://code.google.com/p/protobuf/
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Author: kenton@google.com (Kenton Varda)
//  Based on original Protocol Buffers design by
//  Sanjay Ghemawat, Jeff Dean, and others.
//
// To test GeneratedMessageReflection, we actually let the protocol compiler
// generate a full protocol message implementation and then test its
// reflection interface.  This is much easier and more maintainable than
// trying to create our own Message class for GeneratedMessageReflection
// to wrap.
//
// The tests here closely mirror some of the tests in
// compiler/cpp/unittest, except using the reflection interface
// rather than generated accessors.

#include <google/protobuf/generated_message_reflection.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/test_util.h>
#include <google/protobuf/unittest.pb.h>

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/testing/googletest.h>
#include <gtest/gtest.h>

namespace google {
namespace protobuf {

namespace {

// Shorthand to get a FieldDescriptor for a field of unittest::TestAllTypes.
const FieldDescriptor* F(const string& name) {
  const FieldDescriptor* result =
    unittest::TestAllTypes::descriptor()->FindFieldByName(name);
  GOOGLE_CHECK(result != NULL);
  return result;
}

TEST(GeneratedMessageReflectionTest, Defaults) {
  // Check that all default values are set correctly in the initial message.
  unittest::TestAllTypes message;
  TestUtil::ReflectionTester reflection_tester(
    unittest::TestAllTypes::descriptor());

  reflection_tester.ExpectClearViaReflection(*message.GetReflection());

  const Message::Reflection& reflection = *message.GetReflection();

  // Messages should return pointers to default instances until first use.
  // (This is not checked by ExpectClear() since it is not actually true after
  // the fields have been set and then cleared.)
  EXPECT_EQ(&unittest::TestAllTypes::OptionalGroup::default_instance(),
            &reflection.GetMessage(F("optionalgroup")));
  EXPECT_EQ(&unittest::TestAllTypes::NestedMessage::default_instance(),
            &reflection.GetMessage(F("optional_nested_message")));
  EXPECT_EQ(&unittest::ForeignMessage::default_instance(),
            &reflection.GetMessage(F("optional_foreign_message")));
  EXPECT_EQ(&unittest_import::ImportMessage::default_instance(),
            &reflection.GetMessage(F("optional_import_message")));
}

TEST(GeneratedMessageReflectionTest, Accessors) {
  // Set every field to a unique value then go back and check all those
  // values.
  unittest::TestAllTypes message;
  TestUtil::ReflectionTester reflection_tester(
    unittest::TestAllTypes::descriptor());

  reflection_tester.SetAllFieldsViaReflection(message.GetReflection());
  TestUtil::ExpectAllFieldsSet(message);
  reflection_tester.ExpectAllFieldsSetViaReflection(*message.GetReflection());

  reflection_tester.ModifyRepeatedFieldsViaReflection(message.GetReflection());
  TestUtil::ExpectRepeatedFieldsModified(message);
}

TEST(GeneratedMessageReflectionTest, GetStringReference) {
  // Test that GetStringReference() returns the underlying string when it is
  // a normal string field.
  unittest::TestAllTypes message;
  message.set_optional_string("foo");
  message.add_repeated_string("foo");

  const Message::Reflection& reflection = *message.GetReflection();
  string scratch;

  EXPECT_EQ(&message.optional_string(),
      &reflection.GetStringReference(F("optional_string"), &scratch))
    << "For simple string fields, GetStringReference() should return a "
       "reference to the underlying string.";
  EXPECT_EQ(&message.repeated_string(0),
      &reflection.GetRepeatedStringReference(F("repeated_string"), 0, &scratch))
    << "For simple string fields, GetRepeatedStringReference() should return "
       "a reference to the underlying string.";
}


TEST(GeneratedMessageReflectionTest, DefaultsAfterClear) {
  // Check that after setting all fields and then clearing, getting an
  // embedded message does NOT return the default instance.
  unittest::TestAllTypes message;
  TestUtil::ReflectionTester reflection_tester(
    unittest::TestAllTypes::descriptor());

  TestUtil::SetAllFields(&message);
  message.Clear();

  const Message::Reflection& reflection = *message.GetReflection();

  EXPECT_NE(&unittest::TestAllTypes::OptionalGroup::default_instance(),
            &reflection.GetMessage(F("optionalgroup")));
  EXPECT_NE(&unittest::TestAllTypes::NestedMessage::default_instance(),
            &reflection.GetMessage(F("optional_nested_message")));
  EXPECT_NE(&unittest::ForeignMessage::default_instance(),
            &reflection.GetMessage(F("optional_foreign_message")));
  EXPECT_NE(&unittest_import::ImportMessage::default_instance(),
            &reflection.GetMessage(F("optional_import_message")));
}

TEST(GeneratedMessageReflectionTest, Extensions) {
  // Set every extension to a unique value then go back and check all those
  // values.
  unittest::TestAllExtensions message;
  TestUtil::ReflectionTester reflection_tester(
    unittest::TestAllExtensions::descriptor());

  reflection_tester.SetAllFieldsViaReflection(message.GetReflection());
  TestUtil::ExpectAllExtensionsSet(message);
  reflection_tester.ExpectAllFieldsSetViaReflection(*message.GetReflection());

  reflection_tester.ModifyRepeatedFieldsViaReflection(message.GetReflection());
  TestUtil::ExpectRepeatedExtensionsModified(message);
}

TEST(GeneratedMessageReflectionTest, FindExtensionTypeByNumber) {
  const Message::Reflection& reflection =
    *unittest::TestAllExtensions::default_instance().GetReflection();

  const FieldDescriptor* extension1 =
    unittest::TestAllExtensions::descriptor()->file()->FindExtensionByName(
      "optional_int32_extension");
  const FieldDescriptor* extension2 =
    unittest::TestAllExtensions::descriptor()->file()->FindExtensionByName(
      "repeated_string_extension");

  EXPECT_EQ(extension1,
            reflection.FindKnownExtensionByNumber(extension1->number()));
  EXPECT_EQ(extension2,
            reflection.FindKnownExtensionByNumber(extension2->number()));

  // Non-existent extension.
  EXPECT_TRUE(reflection.FindKnownExtensionByNumber(62341) == NULL);

  // Extensions of TestAllExtensions should not show up as extensions of
  // other types.
  EXPECT_TRUE(unittest::TestAllTypes::default_instance().GetReflection()->
              FindKnownExtensionByNumber(extension1->number()) == NULL);
}

TEST(GeneratedMessageReflectionTest, FindKnownExtensionByName) {
  const Message::Reflection& reflection =
    *unittest::TestAllExtensions::default_instance().GetReflection();

  const FieldDescriptor* extension1 =
    unittest::TestAllExtensions::descriptor()->file()->FindExtensionByName(
      "optional_int32_extension");
  const FieldDescriptor* extension2 =
    unittest::TestAllExtensions::descriptor()->file()->FindExtensionByName(
      "repeated_string_extension");

  EXPECT_EQ(extension1,
            reflection.FindKnownExtensionByName(extension1->full_name()));
  EXPECT_EQ(extension2,
            reflection.FindKnownExtensionByName(extension2->full_name()));

  // Non-existent extension.
  EXPECT_TRUE(reflection.FindKnownExtensionByName("no_such_ext") == NULL);

  // Extensions of TestAllExtensions should not show up as extensions of
  // other types.
  EXPECT_TRUE(unittest::TestAllTypes::default_instance().GetReflection()->
              FindKnownExtensionByName(extension1->full_name()) == NULL);
}

#ifdef GTEST_HAS_DEATH_TEST

TEST(GeneratedMessageReflectionTest, UsageErrors) {
  unittest::TestAllTypes message;
  Message::Reflection* reflection = message.GetReflection();
  const Descriptor* descriptor = message.GetDescriptor();

#define f(NAME) descriptor->FindFieldByName(NAME)

  // Testing every single failure mode would be too much work.  Let's just
  // check a few.
  EXPECT_DEATH(
    reflection->GetInt32(descriptor->FindFieldByName("optional_int64")),
    "Protocol Buffer reflection usage error:\n"
    "  Method      : google::protobuf::Message::Reflection::GetInt32\n"
    "  Message type: protobuf_unittest\\.TestAllTypes\n"
    "  Field       : protobuf_unittest\\.TestAllTypes\\.optional_int64\n"
    "  Problem     : Field is not the right type for this message:\n"
    "    Expected  : CPPTYPE_INT32\n"
    "    Field type: CPPTYPE_INT64");
  EXPECT_DEATH(
    reflection->GetInt32(descriptor->FindFieldByName("repeated_int32")),
    "Protocol Buffer reflection usage error:\n"
    "  Method      : google::protobuf::Message::Reflection::GetInt32\n"
    "  Message type: protobuf_unittest.TestAllTypes\n"
    "  Field       : protobuf_unittest.TestAllTypes.repeated_int32\n"
    "  Problem     : Field is repeated; the method requires a singular field.");
  EXPECT_DEATH(
    reflection->GetInt32(
      unittest::ForeignMessage::descriptor()->FindFieldByName("c")),
    "Protocol Buffer reflection usage error:\n"
    "  Method      : google::protobuf::Message::Reflection::GetInt32\n"
    "  Message type: protobuf_unittest.TestAllTypes\n"
    "  Field       : protobuf_unittest.ForeignMessage.c\n"
    "  Problem     : Field does not match message type.");
  EXPECT_DEATH(
    reflection->HasField(
      unittest::ForeignMessage::descriptor()->FindFieldByName("c")),
    "Protocol Buffer reflection usage error:\n"
    "  Method      : google::protobuf::Message::Reflection::HasField\n"
    "  Message type: protobuf_unittest.TestAllTypes\n"
    "  Field       : protobuf_unittest.ForeignMessage.c\n"
    "  Problem     : Field does not match message type.");

#undef f
}

#endif  // GTEST_HAS_DEATH_TEST


}  // namespace
}  // namespace protobuf
}  // namespace google
