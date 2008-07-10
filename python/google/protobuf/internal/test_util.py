# Protocol Buffers - Google's data interchange format
# Copyright 2008 Google Inc.
# http://code.google.com/p/protobuf/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Utilities for Python proto2 tests.

This is intentionally modeled on C++ code in
//net/proto2/internal/test_util.*.
"""

__author__ = 'robinson@google.com (Will Robinson)'

import os.path

from google.protobuf import unittest_import_pb2
from google.protobuf import unittest_pb2


def SetAllFields(message):
  """Sets every field in the message to a unique value.

  Args:
    message: A unittest_pb2.TestAllTypes instance.
  """

  #
  # Optional fields.
  #

  message.optional_int32    = 101
  message.optional_int64    = 102
  message.optional_uint32   = 103
  message.optional_uint64   = 104
  message.optional_sint32   = 105
  message.optional_sint64   = 106
  message.optional_fixed32  = 107
  message.optional_fixed64  = 108
  message.optional_sfixed32 = 109
  message.optional_sfixed64 = 110
  message.optional_float    = 111
  message.optional_double   = 112
  message.optional_bool     = True
  # TODO(robinson): Firmly spec out and test how
  # protos interact with unicode.  One specific example:
  # what happens if we change the literal below to
  # u'115'?  What *should* happen?  Still some discussion
  # to finish with Kenton about bytes vs. strings
  # and forcing everything to be utf8. :-/
  message.optional_string   = '115'
  message.optional_bytes    = '116'

  message.optionalgroup.a = 117
  message.optional_nested_message.bb = 118
  message.optional_foreign_message.c = 119
  message.optional_import_message.d = 120

  message.optional_nested_enum = unittest_pb2.TestAllTypes.BAZ
  message.optional_foreign_enum = unittest_pb2.FOREIGN_BAZ
  message.optional_import_enum = unittest_import_pb2.IMPORT_BAZ

  message.optional_string_piece = '124'
  message.optional_cord = '125'

  #
  # Repeated fields.
  #

  message.repeated_int32.append(201)
  message.repeated_int64.append(202)
  message.repeated_uint32.append(203)
  message.repeated_uint64.append(204)
  message.repeated_sint32.append(205)
  message.repeated_sint64.append(206)
  message.repeated_fixed32.append(207)
  message.repeated_fixed64.append(208)
  message.repeated_sfixed32.append(209)
  message.repeated_sfixed64.append(210)
  message.repeated_float.append(211)
  message.repeated_double.append(212)
  message.repeated_bool.append(True)
  message.repeated_string.append('215')
  message.repeated_bytes.append('216')

  message.repeatedgroup.add().a = 217
  message.repeated_nested_message.add().bb = 218
  message.repeated_foreign_message.add().c = 219
  message.repeated_import_message.add().d = 220

  message.repeated_nested_enum.append(unittest_pb2.TestAllTypes.BAR)
  message.repeated_foreign_enum.append(unittest_pb2.FOREIGN_BAR)
  message.repeated_import_enum.append(unittest_import_pb2.IMPORT_BAR)

  message.repeated_string_piece.append('224')
  message.repeated_cord.append('225')

  # Add a second one of each field.
  message.repeated_int32.append(301)
  message.repeated_int64.append(302)
  message.repeated_uint32.append(303)
  message.repeated_uint64.append(304)
  message.repeated_sint32.append(305)
  message.repeated_sint64.append(306)
  message.repeated_fixed32.append(307)
  message.repeated_fixed64.append(308)
  message.repeated_sfixed32.append(309)
  message.repeated_sfixed64.append(310)
  message.repeated_float.append(311)
  message.repeated_double.append(312)
  message.repeated_bool.append(False)
  message.repeated_string.append('315')
  message.repeated_bytes.append('316')

  message.repeatedgroup.add().a = 317
  message.repeated_nested_message.add().bb = 318
  message.repeated_foreign_message.add().c = 319
  message.repeated_import_message.add().d = 320

  message.repeated_nested_enum.append(unittest_pb2.TestAllTypes.BAZ)
  message.repeated_foreign_enum.append(unittest_pb2.FOREIGN_BAZ)
  message.repeated_import_enum.append(unittest_import_pb2.IMPORT_BAZ)

  message.repeated_string_piece.append('324')
  message.repeated_cord.append('325')

  #
  # Fields that have defaults.
  #

  message.default_int32 = 401
  message.default_int64 = 402
  message.default_uint32 = 403
  message.default_uint64 = 404
  message.default_sint32 = 405
  message.default_sint64 = 406
  message.default_fixed32 = 407
  message.default_fixed64 = 408
  message.default_sfixed32 = 409
  message.default_sfixed64 = 410
  message.default_float = 411
  message.default_double = 412
  message.default_bool = False
  message.default_string = '415'
  message.default_bytes = '416'

  message.default_nested_enum = unittest_pb2.TestAllTypes.FOO
  message.default_foreign_enum = unittest_pb2.FOREIGN_FOO
  message.default_import_enum = unittest_import_pb2.IMPORT_FOO

  message.default_string_piece = '424'
  message.default_cord = '425'


def SetAllExtensions(message):
  """Sets every extension in the message to a unique value.

  Args:
    message: A unittest_pb2.TestAllExtensions instance.
  """

  extensions = message.Extensions
  pb2 = unittest_pb2
  import_pb2 = unittest_import_pb2

  #
  # Optional fields.
  #

  extensions[pb2.optional_int32_extension] = 101
  extensions[pb2.optional_int64_extension] = 102
  extensions[pb2.optional_uint32_extension] = 103
  extensions[pb2.optional_uint64_extension] = 104
  extensions[pb2.optional_sint32_extension] = 105
  extensions[pb2.optional_sint64_extension] = 106
  extensions[pb2.optional_fixed32_extension] = 107
  extensions[pb2.optional_fixed64_extension] = 108
  extensions[pb2.optional_sfixed32_extension] = 109
  extensions[pb2.optional_sfixed64_extension] = 110
  extensions[pb2.optional_float_extension] = 111
  extensions[pb2.optional_double_extension] = 112
  extensions[pb2.optional_bool_extension] = True
  extensions[pb2.optional_string_extension] = '115'
  extensions[pb2.optional_bytes_extension] = '116'

  extensions[pb2.optionalgroup_extension].a = 117
  extensions[pb2.optional_nested_message_extension].bb = 118
  extensions[pb2.optional_foreign_message_extension].c = 119
  extensions[pb2.optional_import_message_extension].d = 120

  extensions[pb2.optional_nested_enum_extension] = pb2.TestAllTypes.BAZ
  extensions[pb2.optional_nested_enum_extension] = pb2.TestAllTypes.BAZ
  extensions[pb2.optional_foreign_enum_extension] = pb2.FOREIGN_BAZ
  extensions[pb2.optional_import_enum_extension] = import_pb2.IMPORT_BAZ

  extensions[pb2.optional_string_piece_extension] = '124'
  extensions[pb2.optional_cord_extension] = '125'

  #
  # Repeated fields.
  #

  extensions[pb2.repeated_int32_extension].append(201)
  extensions[pb2.repeated_int64_extension].append(202)
  extensions[pb2.repeated_uint32_extension].append(203)
  extensions[pb2.repeated_uint64_extension].append(204)
  extensions[pb2.repeated_sint32_extension].append(205)
  extensions[pb2.repeated_sint64_extension].append(206)
  extensions[pb2.repeated_fixed32_extension].append(207)
  extensions[pb2.repeated_fixed64_extension].append(208)
  extensions[pb2.repeated_sfixed32_extension].append(209)
  extensions[pb2.repeated_sfixed64_extension].append(210)
  extensions[pb2.repeated_float_extension].append(211)
  extensions[pb2.repeated_double_extension].append(212)
  extensions[pb2.repeated_bool_extension].append(True)
  extensions[pb2.repeated_string_extension].append('215')
  extensions[pb2.repeated_bytes_extension].append('216')

  extensions[pb2.repeatedgroup_extension].add().a = 217
  extensions[pb2.repeated_nested_message_extension].add().bb = 218
  extensions[pb2.repeated_foreign_message_extension].add().c = 219
  extensions[pb2.repeated_import_message_extension].add().d = 220

  extensions[pb2.repeated_nested_enum_extension].append(pb2.TestAllTypes.BAR)
  extensions[pb2.repeated_foreign_enum_extension].append(pb2.FOREIGN_BAR)
  extensions[pb2.repeated_import_enum_extension].append(import_pb2.IMPORT_BAR)

  extensions[pb2.repeated_string_piece_extension].append('224')
  extensions[pb2.repeated_cord_extension].append('225')

  # Append a second one of each field.
  extensions[pb2.repeated_int32_extension].append(301)
  extensions[pb2.repeated_int64_extension].append(302)
  extensions[pb2.repeated_uint32_extension].append(303)
  extensions[pb2.repeated_uint64_extension].append(304)
  extensions[pb2.repeated_sint32_extension].append(305)
  extensions[pb2.repeated_sint64_extension].append(306)
  extensions[pb2.repeated_fixed32_extension].append(307)
  extensions[pb2.repeated_fixed64_extension].append(308)
  extensions[pb2.repeated_sfixed32_extension].append(309)
  extensions[pb2.repeated_sfixed64_extension].append(310)
  extensions[pb2.repeated_float_extension].append(311)
  extensions[pb2.repeated_double_extension].append(312)
  extensions[pb2.repeated_bool_extension].append(False)
  extensions[pb2.repeated_string_extension].append('315')
  extensions[pb2.repeated_bytes_extension].append('316')

  extensions[pb2.repeatedgroup_extension].add().a = 317
  extensions[pb2.repeated_nested_message_extension].add().bb = 318
  extensions[pb2.repeated_foreign_message_extension].add().c = 319
  extensions[pb2.repeated_import_message_extension].add().d = 320

  extensions[pb2.repeated_nested_enum_extension].append(pb2.TestAllTypes.BAZ)
  extensions[pb2.repeated_foreign_enum_extension].append(pb2.FOREIGN_BAZ)
  extensions[pb2.repeated_import_enum_extension].append(import_pb2.IMPORT_BAZ)

  extensions[pb2.repeated_string_piece_extension].append('324')
  extensions[pb2.repeated_cord_extension].append('325')

  #
  # Fields with defaults.
  #

  extensions[pb2.default_int32_extension] = 401
  extensions[pb2.default_int64_extension] = 402
  extensions[pb2.default_uint32_extension] = 403
  extensions[pb2.default_uint64_extension] = 404
  extensions[pb2.default_sint32_extension] = 405
  extensions[pb2.default_sint64_extension] = 406
  extensions[pb2.default_fixed32_extension] = 407
  extensions[pb2.default_fixed64_extension] = 408
  extensions[pb2.default_sfixed32_extension] = 409
  extensions[pb2.default_sfixed64_extension] = 410
  extensions[pb2.default_float_extension] = 411
  extensions[pb2.default_double_extension] = 412
  extensions[pb2.default_bool_extension] = False
  extensions[pb2.default_string_extension] = '415'
  extensions[pb2.default_bytes_extension] = '416'

  extensions[pb2.default_nested_enum_extension] = pb2.TestAllTypes.FOO
  extensions[pb2.default_foreign_enum_extension] = pb2.FOREIGN_FOO
  extensions[pb2.default_import_enum_extension] = import_pb2.IMPORT_FOO

  extensions[pb2.default_string_piece_extension] = '424'
  extensions[pb2.default_cord_extension] = '425'


def SetAllFieldsAndExtensions(message):
  """Sets every field and extension in the message to a unique value.

  Args:
    message: A unittest_pb2.TestAllExtensions message.
  """
  message.my_int = 1
  message.my_string = 'foo'
  message.my_float = 1.0
  message.Extensions[unittest_pb2.my_extension_int] = 23
  message.Extensions[unittest_pb2.my_extension_string] = 'bar'


def ExpectAllFieldsAndExtensionsInOrder(serialized):
  """Ensures that serialized is the serialization we expect for a message
  filled with SetAllFieldsAndExtensions().  (Specifically, ensures that the
  serialization is in canonical, tag-number order).
  """
  my_extension_int = unittest_pb2.my_extension_int
  my_extension_string = unittest_pb2.my_extension_string
  expected_strings = []
  message = unittest_pb2.TestFieldOrderings()
  message.my_int = 1  # Field 1.
  expected_strings.append(message.SerializeToString())
  message.Clear()
  message.Extensions[my_extension_int] = 23  # Field 5.
  expected_strings.append(message.SerializeToString())
  message.Clear()
  message.my_string = 'foo'  # Field 11.
  expected_strings.append(message.SerializeToString())
  message.Clear()
  message.Extensions[my_extension_string] = 'bar'  # Field 50.
  expected_strings.append(message.SerializeToString())
  message.Clear()
  message.my_float = 1.0
  expected_strings.append(message.SerializeToString())
  message.Clear()
  expected = ''.join(expected_strings)

  if expected != serialized:
    raise ValueError('Expected %r, found %r' % (expected, serialized))

def GoldenFile(filename):
  """Finds the given golden file and returns a file object representing it."""

  # Search up the directory tree looking for the C++ protobuf source code.
  path = '.'
  while os.path.exists(path):
    if os.path.exists(os.path.join(path, 'src/google/protobuf')):
      # Found it.  Load the golden file from the testdata directory.
      return file(os.path.join(path, 'src/google/protobuf/testdata', filename))
    path = os.path.join(path, '..')

  raise RuntimeError(
    'Could not find golden files.  This test must be run from within the '
    'protobuf source package so that it can read test data files from the '
    'C++ source tree.')
