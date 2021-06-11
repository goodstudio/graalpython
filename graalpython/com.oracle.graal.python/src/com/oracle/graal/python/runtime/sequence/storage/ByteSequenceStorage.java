/*
 * Copyright (c) 2017, 2021, Oracle and/or its affiliates.
 * Copyright (c) 2013, Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.graal.python.runtime.sequence.storage;

import static com.oracle.graal.python.runtime.exception.PythonErrorType.TypeError;
import static com.oracle.graal.python.runtime.exception.PythonErrorType.ValueError;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InvalidBufferOffsetException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.ExportMessage.Ignore;
import com.oracle.truffle.api.memory.ByteArraySupport;

@ExportLibrary(BufferStorageLibrary.class)
public final class ByteSequenceStorage extends TypedSequenceStorage {

    private byte[] values;

    public ByteSequenceStorage(byte[] elements) {
        this(elements, elements.length);
    }

    public ByteSequenceStorage(byte[] elements, int length) {
        this.values = elements;
        this.capacity = values.length;
        this.length = length;
    }

    public ByteSequenceStorage(int capacity) {
        this.values = new byte[capacity];
        this.capacity = capacity;
        this.length = 0;
    }

    @Override
    protected void increaseCapacityExactWithCopy(int newCapacity) {
        values = Arrays.copyOf(values, newCapacity);
        capacity = values.length;
    }

    @Override
    protected void increaseCapacityExact(int newCapacity) {
        values = new byte[newCapacity];
        capacity = values.length;
    }

    @Override
    public SequenceStorage copy() {
        return new ByteSequenceStorage(Arrays.copyOf(values, length));
    }

    @Override
    public SequenceStorage createEmpty(int newCapacity) {
        return new ByteSequenceStorage(newCapacity);
    }

    @Override
    public Object[] getInternalArray() {
        /**
         * Have to box and copy.
         */
        Object[] boxed = new Object[length];

        for (int i = 0; i < length; i++) {
            boxed[i] = values[i];
        }

        return boxed;
    }

    @TruffleBoundary(allowInlining = true)
    @Ignore
    public byte[] getInternalByteArray() {
        if (length != values.length) {
            assert length < values.length;
            return Arrays.copyOf(values, length);
        }
        return values;
    }

    @TruffleBoundary(allowInlining = true, transferToInterpreterOnException = false)
    public ByteBuffer getBufferView() {
        ByteBuffer view = ByteBuffer.wrap(values);
        view.limit(values.length);
        return view;
    }

    @Override
    public Object getItemNormalized(int idx) {
        return getIntItemNormalized(idx);
    }

    public final byte getByteItemNormalized(int idx) {
        return values[idx];
    }

    public int getIntItemNormalized(int idx) {
        return values[idx] & 0xFF;
    }

    @Override
    public void setItemNormalized(int idx, Object value) throws SequenceStoreException {
        if (value instanceof Byte) {
            setByteItemNormalized(idx, (byte) value);
        } else if (value instanceof Integer) {
            if ((int) value < 0 || (int) value >= 256) {
                throw PythonLanguage.getCore().raise(ValueError, ErrorMessages.BYTE_MUST_BE_IN_RANGE);
            }
            setByteItemNormalized(idx, ((Integer) value).byteValue());
        } else {
            throw PythonLanguage.getCore().raise(TypeError, ErrorMessages.INTEGER_REQUIRED);
        }
    }

    public void setByteItemNormalized(int idx, byte value) {
        values[idx] = value;
    }

    @Override
    public void insertItem(int idx, Object value) throws SequenceStoreException {
        if (value instanceof Byte) {
            insertByteItem(idx, (byte) value);
        } else if (value instanceof Integer) {
            insertByteItem(idx, ((Integer) value).byteValue());
        } else {
            throw new SequenceStoreException(value);
        }
    }

    public void insertByteItem(int idx, byte value) {
        ensureCapacity(length + 1);

        // shifting tail to the right by one slot
        for (int i = values.length - 1; i > idx; i--) {
            values[i] = values[i - 1];
        }

        values[idx] = value;
        length++;
    }

    @Override
    public void copyItem(int idxTo, int idxFrom) {
        values[idxTo] = values[idxFrom];
    }

    @Override
    public ByteSequenceStorage getSliceInBound(int start, int stop, int step, int sliceLength) {
        byte[] newArray = new byte[sliceLength];

        if (step == 1) {
            PythonUtils.arraycopy(values, start, newArray, 0, sliceLength);
            return new ByteSequenceStorage(newArray);
        }

        for (int i = start, j = 0; j < sliceLength; i += step, j++) {
            newArray[j] = values[i];
        }

        return new ByteSequenceStorage(newArray);
    }

    @TruffleBoundary
    public void setByteSliceInBound(int start, int stop, int step, IntSequenceStorage sequence) {
        int otherLength = sequence.length();
        int[] seqValues = sequence.getInternalIntArray();

        // (stop - start) = bytes to be replaced; otherLength = bytes to be written
        int newLength = length - (stop - start - otherLength);

        ensureCapacity(newLength);

        // if enlarging, we need to move the suffix first
        if (stop - start < otherLength) {
            assert length < newLength;
            for (int j = length - 1, k = newLength - 1; j >= stop; j--, k--) {
                values[k] = values[j];
            }
        }

        int i = start;
        for (int j = 0; j < otherLength; i += step, j++) {
            if (seqValues[j] < Byte.MIN_VALUE || seqValues[j] > Byte.MAX_VALUE) {
                throw PythonLanguage.getCore().raise(ValueError, ErrorMessages.BYTE_MUST_BE_IN_RANGE);
            }
            values[i] = (byte) seqValues[j];
        }

        // if shrinking, move the suffix afterwards
        if (stop - start > otherLength) {
            assert stop >= 0;
            for (int j = i, k = 0; stop + k < values.length; j++, k++) {
                values[j] = values[stop + k];
            }
        }

        // for security
        Arrays.fill(values, newLength, values.length, (byte) 0);

        length = newLength;
    }

    @TruffleBoundary
    public void setByteSliceInBound(int start, int stop, int step, ByteSequenceStorage sequence) {
        int otherLength = sequence.length();

        // range is the whole sequence?
        if (start == 0 && stop == length) {
            values = Arrays.copyOf(sequence.values, otherLength);
            length = otherLength;
            minimizeCapacity();
            return;
        }

        // (stop - start) = bytes to be replaced; otherLength = bytes to be written
        int newLength = length - (stop - start - otherLength);

        ensureCapacity(newLength);

        // if enlarging, we need to move the suffix first
        if (stop - start < otherLength) {
            assert length < newLength;
            for (int j = length - 1, k = newLength - 1; j >= stop; j--, k--) {
                values[k] = values[j];
            }
        }

        int i = start;
        for (int j = 0; j < otherLength; i += step, j++) {
            values[i] = sequence.values[j];
        }

        // if shrinking, move the suffix afterwards
        if (stop - start > otherLength) {
            assert stop >= 0;
            for (int j = i, k = 0; stop + k < values.length; j++, k++) {
                values[j] = values[stop + k];
            }
        }

        // for security
        Arrays.fill(values, newLength, values.length, (byte) 0);

        length = newLength;
    }

    public int popInt() {
        int pop = values[capacity - 1] & 0xFF;
        length--;
        return pop;
    }

    public int indexOfByte(byte value) {
        for (int i = 0; i < length; i++) {
            if (values[i] == value) {
                return i;
            }
        }

        return -1;
    }

    public int indexOfInt(int value) {
        for (int i = 0; i < length; i++) {
            if ((values[i] & 0xFF) == value) {
                return i;
            }
        }

        return -1;
    }

    public void appendLong(long value) {
        if (value < 0 || value >= 256) {
            throw new SequenceStoreException(value);
        }
        ensureCapacity(length + 1);
        values[length] = (byte) value;
        length++;
    }

    public void appendInt(int value) {
        if (value < 0 || value >= 256) {
            throw new SequenceStoreException(value);
        }
        ensureCapacity(length + 1);
        values[length] = (byte) value;
        length++;
    }

    public void appendByte(byte value) {
        ensureCapacity(length + 1);
        values[length] = value;
        length++;
    }

    @Override
    public void reverse() {
        if (length > 0) {
            int head = 0;
            int tail = length - 1;
            int middle = (length - 1) / 2;

            for (; head <= middle; head++, tail--) {
                byte temp = values[head];
                values[head] = values[tail];
                values[tail] = temp;
            }
        }
    }

    @Override
    public Object getIndicativeValue() {
        return 0;
    }

    @Override
    public boolean equals(SequenceStorage other) {
        if (other.length() != length() || !(other instanceof ByteSequenceStorage)) {
            return false;
        }

        byte[] otherArray = ((ByteSequenceStorage) other).getInternalByteArray();
        for (int i = 0; i < length(); i++) {
            if (values[i] != otherArray[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object getInternalArrayObject() {
        return values;
    }

    @Override
    public Object getCopyOfInternalArrayObject() {
        return Arrays.copyOf(values, length);
    }

    @Override
    public Object[] getCopyOfInternalArray() {
        return getInternalArray();
    }

    @Override
    public void setInternalArrayObject(Object arrayObject) {
        this.values = (byte[]) arrayObject;
    }

    @Override
    public ListStorageType getElementType() {
        return ListStorageType.Byte;
    }

    @ExportMessage
    int getBufferLength() {
        return length;
    }

    @ExportMessage
    @SuppressWarnings("static-method")
    boolean hasInternalByteArray() {
        return true;
    }

    @ExportMessage(name = "getInternalByteArray")
    byte[] getInternalByteArrayMessage() {
        return values;
    }

    @ExportMessage
    void copyFrom(int srcOffset, byte[] dest, int destOffset, int copyLength) {
        PythonUtils.arraycopy(values, srcOffset, dest, destOffset, copyLength);
    }

    @ExportMessage
    void copyTo(int destOffset, byte[] src, int srcOffset, int copyLength) {
        PythonUtils.arraycopy(src, srcOffset, values, destOffset, copyLength);
    }

    private void checkOffset(long byteOffset, int elementLen) throws InvalidBufferOffsetException {
        if (byteOffset < 0 || byteOffset + elementLen - 1 >= length) {
            throw InvalidBufferOffsetException.create(byteOffset, length);
        }
    }

    @ExportMessage
    byte readBufferByte(long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 1);
        return values[(int) byteOffset];
    }

    @ExportMessage
    void writeBufferByte(long byteOffset, byte value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 1);
        values[(int) byteOffset] = value;
    }

    private static ByteArraySupport getByteArraySupport(ByteOrder order) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return ByteArraySupport.littleEndian();
        } else {
            return ByteArraySupport.bigEndian();
        }
    }

    @ExportMessage
    short readBufferShort(ByteOrder order, long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 2);
        return getByteArraySupport(order).getShort(values, (int) byteOffset);
    }

    @ExportMessage
    void writeBufferShort(ByteOrder order, long byteOffset, short value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 2);
        getByteArraySupport(order).putShort(values, (int) byteOffset, value);
    }

    @ExportMessage
    int readBufferInt(ByteOrder order, long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 4);
        return getByteArraySupport(order).getInt(values, (int) byteOffset);
    }

    @ExportMessage
    void writeBufferInt(ByteOrder order, long byteOffset, int value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 4);
        getByteArraySupport(order).putInt(values, (int) byteOffset, value);
    }

    @ExportMessage
    long readBufferLong(ByteOrder order, long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 8);
        return getByteArraySupport(order).getLong(values, (int) byteOffset);
    }

    @ExportMessage
    void writeBufferLong(ByteOrder order, long byteOffset, long value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 8);
        getByteArraySupport(order).putLong(values, (int) byteOffset, value);
    }

    @ExportMessage
    float readBufferFloat(ByteOrder order, long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 4);
        return getByteArraySupport(order).getFloat(values, (int) byteOffset);
    }

    @ExportMessage
    void writeBufferFloat(ByteOrder order, long byteOffset, float value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 4);
        getByteArraySupport(order).putFloat(values, (int) byteOffset, value);
    }

    @ExportMessage
    double readBufferDouble(ByteOrder order, long byteOffset) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 8);
        return getByteArraySupport(order).getDouble(values, (int) byteOffset);
    }

    @ExportMessage
    void writeBufferDouble(ByteOrder order, long byteOffset, double value) throws InvalidBufferOffsetException {
        checkOffset(byteOffset, 8);
        getByteArraySupport(order).putDouble(values, (int) byteOffset, value);
    }
}
