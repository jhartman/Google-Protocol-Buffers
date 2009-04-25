// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.google.protobuf;

import com.google.protobuf.Descriptors.FieldDescriptor;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * A partial implementation of the {@link Message} interface which implements
 * as many methods of that interface as possible in terms of other methods.
 *
 * @author kenton@google.com Kenton Varda
 */
public abstract class AbstractMessage implements Message {
  @SuppressWarnings("unchecked")
  public boolean isInitialized() {
    // Check that all required fields are present.
    for (FieldDescriptor field : getDescriptorForType().getFields()) {
      if (field.isRequired()) {
        if (!hasField(field)) {
          return false;
        }
      }
    }

    // Check that embedded messages are initialized.
    for (Map.Entry<FieldDescriptor, Object> entry : getAllFields().entrySet()) {
      FieldDescriptor field = entry.getKey();
      if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
        if (field.isRepeated()) {
          for (Message element : (List<Message>) entry.getValue()) {
            if (!element.isInitialized()) {
              return false;
            }
          }
        } else {
          if (!((Message) entry.getValue()).isInitialized()) {
            return false;
          }
        }
      }
    }

    return true;
  }

  @Override
  public final String toString() {
    return TextFormat.printToString(this);
  }

  public void writeTo(CodedOutputStream output) throws IOException {
    for (Map.Entry<FieldDescriptor, Object> entry : getAllFields().entrySet()) {
      FieldDescriptor field = entry.getKey();
      if (field.isRepeated()) {
        List valueList = (List) entry.getValue();
        if (field.getOptions().getPacked()) {

          output.writeTag(field.getNumber(),
                          WireFormat.WIRETYPE_LENGTH_DELIMITED);
          int dataSize = 0;
          for (Object element : valueList) {
            dataSize += CodedOutputStream.computeFieldSizeNoTag(
                field.getType(), element);
          }
          output.writeRawVarint32(dataSize);

          for (Object element : valueList) {
            output.writeFieldNoTag(field.getType(), element);
          }
        } else {
          for (Object element : valueList) {
            output.writeField(field.getType(), field.getNumber(), element);
          }
        }
      } else {
        output.writeField(field.getType(), field.getNumber(), entry.getValue());
      }
    }

    UnknownFieldSet unknownFields = getUnknownFields();
    if (getDescriptorForType().getOptions().getMessageSetWireFormat()) {
      unknownFields.writeAsMessageSetTo(output);
    } else {
      unknownFields.writeTo(output);
    }
  }

  public ByteString toByteString() {
    try {
      ByteString.CodedBuilder out =
        ByteString.newCodedBuilder(getSerializedSize());
      writeTo(out.getCodedOutput());
      return out.build();
    } catch (IOException e) {
      throw new RuntimeException(
        "Serializing to a ByteString threw an IOException (should " +
        "never happen).", e);
    }
  }

  public byte[] toByteArray() {
    try {
      byte[] result = new byte[getSerializedSize()];
      CodedOutputStream output = CodedOutputStream.newInstance(result);
      writeTo(output);
      output.checkNoSpaceLeft();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(
        "Serializing to a byte array threw an IOException " +
        "(should never happen).", e);
    }
  }

  public void writeTo(OutputStream output) throws IOException {
    CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
    writeTo(codedOutput);
    codedOutput.flush();
  }

  public void writeDelimitedTo(OutputStream output) throws IOException {
    CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
    codedOutput.writeRawVarint32(getSerializedSize());
    writeTo(codedOutput);
    codedOutput.flush();
  }

  private int memoizedSize = -1;

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (Map.Entry<FieldDescriptor, Object> entry : getAllFields().entrySet()) {
      FieldDescriptor field = entry.getKey();
      if (field.isRepeated()) {
        List valueList = (List) entry.getValue();
        if (field.getOptions().getPacked()) {
          int dataSize = 0;
          for (Object element : valueList) {
            dataSize += CodedOutputStream.computeFieldSizeNoTag(
                field.getType(), element);
          }
          size += dataSize;
          size += CodedOutputStream.computeTagSize(field.getNumber());
          size += CodedOutputStream.computeRawVarint32Size(dataSize);
        } else {
          for (Object element : valueList) {
            size += CodedOutputStream.computeFieldSize(
                field.getType(), field.getNumber(), element);
          }
        }
      } else {
        size += CodedOutputStream.computeFieldSize(
          field.getType(), field.getNumber(), entry.getValue());
      }
    }

    UnknownFieldSet unknownFields = getUnknownFields();
    if (getDescriptorForType().getOptions().getMessageSetWireFormat()) {
      size += unknownFields.getSerializedSizeAsMessageSet();
    } else {
      size += unknownFields.getSerializedSize();
    }

    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof Message)) {
      return false;
    }
    Message otherMessage = (Message) other;
    if (getDescriptorForType() != otherMessage.getDescriptorForType()) {
      return false;
    }
    return getAllFields().equals(otherMessage.getAllFields()) &&
        getUnknownFields().equals(otherMessage.getUnknownFields());
  }

  @Override
  public int hashCode() {
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (53 * hash) + getAllFields().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    return hash;
  }

  // =================================================================

  /**
   * A partial implementation of the {@link Message.Builder} interface which
   * implements as many methods of that interface as possible in terms of
   * other methods.
   */
  @SuppressWarnings("unchecked")
  public static abstract class Builder<BuilderType extends Builder>
      implements Message.Builder {
    // The compiler produces an error if this is not declared explicitly.
    @Override
    public abstract BuilderType clone();

    public BuilderType clear() {
      for (Map.Entry<FieldDescriptor, Object> entry :
           getAllFields().entrySet()) {
        clearField(entry.getKey());
      }
      return (BuilderType) this;
    }

    public BuilderType mergeFrom(Message other) {
      if (other.getDescriptorForType() != getDescriptorForType()) {
        throw new IllegalArgumentException(
          "mergeFrom(Message) can only merge messages of the same type.");
      }

      // Note:  We don't attempt to verify that other's fields have valid
      //   types.  Doing so would be a losing battle.  We'd have to verify
      //   all sub-messages as well, and we'd have to make copies of all of
      //   them to insure that they don't change after verification (since
      //   the Message interface itself cannot enforce immutability of
      //   implementations).
      // TODO(kenton):  Provide a function somewhere called makeDeepCopy()
      //   which allows people to make secure deep copies of messages.

      for (Map.Entry<FieldDescriptor, Object> entry :
           other.getAllFields().entrySet()) {
        FieldDescriptor field = entry.getKey();
        if (field.isRepeated()) {
          for (Object element : (List)entry.getValue()) {
            addRepeatedField(field, element);
          }
        } else if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
          Message existingValue = (Message)getField(field);
          if (existingValue == existingValue.getDefaultInstanceForType()) {
            setField(field, entry.getValue());
          } else {
            setField(field,
              existingValue.newBuilderForType()
                .mergeFrom(existingValue)
                .mergeFrom((Message)entry.getValue())
                .build());
          }
        } else {
          setField(field, entry.getValue());
        }
      }

      return (BuilderType) this;
    }

    public BuilderType mergeFrom(CodedInputStream input) throws IOException {
      return mergeFrom(input, ExtensionRegistry.getEmptyRegistry());
    }

    public BuilderType mergeFrom(CodedInputStream input,
                                 ExtensionRegistry extensionRegistry)
                                 throws IOException {
      UnknownFieldSet.Builder unknownFields =
        UnknownFieldSet.newBuilder(getUnknownFields());
      FieldSet.mergeFrom(input, unknownFields, extensionRegistry, this);
      setUnknownFields(unknownFields.build());
      return (BuilderType) this;
    }

    public BuilderType mergeUnknownFields(UnknownFieldSet unknownFields) {
      setUnknownFields(
        UnknownFieldSet.newBuilder(getUnknownFields())
                       .mergeFrom(unknownFields)
                       .build());
      return (BuilderType) this;
    }

    public BuilderType mergeFrom(ByteString data)
        throws InvalidProtocolBufferException {
      try {
        CodedInputStream input = data.newCodedInput();
        mergeFrom(input);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(
          "Reading from a ByteString threw an IOException (should " +
          "never happen).", e);
      }
    }

    public BuilderType mergeFrom(ByteString data,
                                 ExtensionRegistry extensionRegistry)
                                 throws InvalidProtocolBufferException {
      try {
        CodedInputStream input = data.newCodedInput();
        mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(
          "Reading from a ByteString threw an IOException (should " +
          "never happen).", e);
      }
    }

    public BuilderType mergeFrom(byte[] data)
        throws InvalidProtocolBufferException {
      return mergeFrom(data, 0, data.length);
    }

    public BuilderType mergeFrom(byte[] data, int off, int len)
        throws InvalidProtocolBufferException {
      try {
        CodedInputStream input = CodedInputStream.newInstance(data, off, len);
        mergeFrom(input);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(
          "Reading from a byte array threw an IOException (should " +
          "never happen).", e);
      }
    }

    public BuilderType mergeFrom(
        byte[] data,
        ExtensionRegistry extensionRegistry)
        throws InvalidProtocolBufferException {
      return mergeFrom(data, 0, data.length, extensionRegistry);
    }

    public BuilderType mergeFrom(
        byte[] data, int off, int len,
        ExtensionRegistry extensionRegistry)
        throws InvalidProtocolBufferException {
      try {
        CodedInputStream input = CodedInputStream.newInstance(data, off, len);
        mergeFrom(input, extensionRegistry);
        input.checkLastTagWas(0);
        return (BuilderType) this;
      } catch (InvalidProtocolBufferException e) {
        throw e;
      } catch (IOException e) {
        throw new RuntimeException(
          "Reading from a byte array threw an IOException (should " +
          "never happen).", e);
      }
    }

    public BuilderType mergeFrom(InputStream input) throws IOException {
      CodedInputStream codedInput = CodedInputStream.newInstance(input);
      mergeFrom(codedInput);
      codedInput.checkLastTagWas(0);
      return (BuilderType) this;
    }

    public BuilderType mergeFrom(InputStream input,
                                 ExtensionRegistry extensionRegistry)
                                 throws IOException {
      CodedInputStream codedInput = CodedInputStream.newInstance(input);
      mergeFrom(codedInput, extensionRegistry);
      codedInput.checkLastTagWas(0);
      return (BuilderType) this;
    }

    public BuilderType mergeDelimitedFrom(InputStream input,
                                          ExtensionRegistry extensionRegistry)
                                          throws IOException {
      final int size = CodedInputStream.readRawVarint32(input);

      // A stream which will not read more than |size| bytes.
      InputStream limitedInput = new FilterInputStream(input) {
        int limit = size;

        @Override
        public int available() throws IOException {
          return Math.min(super.available(), limit);
        }

        @Override
        public int read() throws IOException {
          if (limit <= 0) return -1;
          int result = super.read();
          if (result >= 0) --limit;
          return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
          if (limit <= 0) return -1;
          len = Math.min(len, limit);
          int result = super.read(b, off, len);
          if (result >= 0) limit -= result;
          return result;
        }

        @Override
        public long skip(long n) throws IOException {
          long result = super.skip(Math.min(n, limit));
          if (result >= 0) limit -= result;
          return result;
        }
      };
      return mergeFrom(limitedInput, extensionRegistry);
    }

    public BuilderType mergeDelimitedFrom(InputStream input)
        throws IOException {
      return mergeDelimitedFrom(input, ExtensionRegistry.getEmptyRegistry());
    }
  }
}
