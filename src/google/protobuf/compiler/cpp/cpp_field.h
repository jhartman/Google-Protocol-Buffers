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

#ifndef GOOGLE_PROTOBUF_COMPILER_CPP_FIELD_H__
#define GOOGLE_PROTOBUF_COMPILER_CPP_FIELD_H__

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/descriptor.h>

namespace google {
namespace protobuf {
  namespace io {
    class Printer;             // printer.h
  }
}

namespace protobuf {
namespace compiler {
namespace cpp {

class FieldGenerator {
 public:
  FieldGenerator() {}
  virtual ~FieldGenerator();

  // Generate lines of code declaring members fields of the message class
  // needed to represent this field.  These are placed inside the message
  // class.
  virtual void GeneratePrivateMembers(io::Printer* printer) const = 0;

  // Generate prototypes for all of the accessor functions related to this
  // field.  These are placed inside the class definition.
  virtual void GenerateAccessorDeclarations(io::Printer* printer) const = 0;

  // Generate inline definitions of accessor functions for this field.
  // These are placed inside the header after all class definitions.
  virtual void GenerateInlineAccessorDefinitions(
    io::Printer* printer) const = 0;

  // Generate definitions of accessors that aren't inlined.  These are
  // placed somewhere in the .cc file.
  // Most field types don't need this, so the default implementation is empty.
  virtual void GenerateNonInlineAccessorDefinitions(
    io::Printer* printer) const {}

  // Generate lines of code (statements, not declarations) which clear the
  // field.  This is used to define the clear_$name$() method as well as
  // the Clear() method for the whole message.
  virtual void GenerateClearingCode(io::Printer* printer) const = 0;

  // Generate lines of code (statements, not declarations) which merges the
  // contents of the field from the current message to the target message,
  // which is stored in the generated code variable "from".
  // This is used to fill in the MergeFrom method for the whole message.
  // Details of this usage can be found in message.cc under the
  // GenerateMergeFrom method.
  virtual void GenerateMergingCode(io::Printer* printer) const = 0;

  // Generate any initializers needed for the private members declared by
  // GeneratePrivateMembers().  These go into the message class's
  // constructor's initializer list.  For each initializer, this method
  // must print the comma and newline separating it from the *previous*
  // initializer, not the *next* initailizer.  That is, print a ",\n" first,
  // e.g.:
  //   printer->Print(",\n$name$_($default$)");
  virtual void GenerateInitializer(io::Printer* printer) const = 0;

  // Generate any code that needs to go in the class's destructor.
  // Most field types don't need this, so the default implementation is empty.
  virtual void GenerateDestructorCode(io::Printer* printer) const {}

  // Generate lines to decode this field, which will be placed inside the
  // message's MergeFromCodedStream() method.
  virtual void GenerateMergeFromCodedStream(io::Printer* printer) const = 0;

  // Generate lines to serialize this field, which are placed within the
  // message's SerializeWithCachedSizes() method.
  virtual void GenerateSerializeWithCachedSizes(io::Printer* printer) const = 0;

  // Generate lines to compute the serialized size of this field, which
  // are placed in the message's ByteSize() method.
  virtual void GenerateByteSize(io::Printer* printer) const = 0;

 private:
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(FieldGenerator);
};

// Convenience class which constructs FieldGenerators for a Descriptor.
class FieldGeneratorMap {
 public:
  explicit FieldGeneratorMap(const Descriptor* descriptor);
  ~FieldGeneratorMap();

  const FieldGenerator& get(const FieldDescriptor* field) const;

 private:
  const Descriptor* descriptor_;
  scoped_array<scoped_ptr<FieldGenerator> > field_generators_;

  static FieldGenerator* MakeGenerator(const FieldDescriptor* field);

  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(FieldGeneratorMap);
};

}  // namespace cpp
}  // namespace compiler
}  // namespace protobuf

}  // namespace google
#endif  // GOOGLE_PROTOBUF_COMPILER_CPP_FIELD_H__
