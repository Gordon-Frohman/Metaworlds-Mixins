// --- BEGIN LICENSE BLOCK ---
/*
 * Copyright (c) 2009-2013, Mikio L. Braun
 * 2011, Nicolas Oury
 * 2013, Alexander Sehlström
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * * Neither the name of the Technische Universitaet Berlin nor the
 * names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// --- END LICENSE BLOCK ---

package org.jblas;

import org.joml.Matrix4d;

public class DoubleMatrix {

    public int rows;
    public int columns;
    public int length;
    public double[] data = null; // rows are contiguous

    public DoubleMatrix(double[] newData) {
        this(newData.length, 1, newData);
    }

    public DoubleMatrix(int newRows, int newColumns, double... newData) {
        rows = newRows;
        columns = newColumns;
        length = rows * columns;

        if (newData != null && newData.length != newRows * newColumns) {
            throw new IllegalArgumentException("Passed data must match matrix dimensions.");
        }

        data = newData;
    }

    public DoubleMatrix(int newRows, int newColumns) {
        this(newRows, newColumns, new double[newRows * newColumns]);
    }

    public static DoubleMatrix eye(int n) {
        DoubleMatrix m = new DoubleMatrix(n, n);

        for (int i = 0; i < n; i++) {
            m.put(i, i, 1.0);
        }

        return m;
    }

    public int index(int rowIndex, int columnIndex) {
        return rowIndex + rows * columnIndex;
    }

    public double get(int rowIndex, int columnIndex) {
        return data[index(rowIndex, columnIndex)];
    }

    public double get(int i) {
        return data[i];
    }

    public DoubleMatrix put(int rowIndex, int columnIndex, double value) {
        data[index(rowIndex, columnIndex)] = value;
        return this;
    }

    public boolean sameSize(DoubleMatrix a) {
        return rows == a.rows && columns == a.columns;
    }

    public void resize(int newRows, int newColumns) {
        rows = newRows;
        columns = newColumns;
        length = newRows * newColumns;
        data = new double[rows * columns];
    }

    public DoubleMatrix copy(DoubleMatrix a) {
        if (!sameSize(a)) {
            resize(a.rows, a.columns);
        }

        System.arraycopy(a.data, 0, data, 0, length);
        return a;
    }

    public DoubleMatrix mmul(DoubleMatrix other) {
        return mmuli(other, new DoubleMatrix(rows, other.columns));
    }

    public DoubleMatrix mmuli(DoubleMatrix other, DoubleMatrix result) {
        if (length == 1 || other.length == 1) {
            throw new RuntimeException("TODO");
        }

        /* check sizes and resize if necessary */
        if (result.rows != rows || result.columns != other.columns) {
            if (result != this && result != other) {
                result.resize(rows, other.columns);
            } else {
                throw new RuntimeException("Cannot resize result matrix because it is used in-place.");
            }
        }

        if (result == this || result == other) {
            /*
             * actually, blas cannot do multiplications in-place. Therefore, we will fake by
             * allocating a temporary object on the side and copy the result later.
             */
            DoubleMatrix temp = new DoubleMatrix(result.rows, result.columns);
            if (other.columns == 1) {
                gemv(1.0, this, other, 0.0, temp);
            } else {
                gemm(1.0, this, other, 0.0, temp);
            }
            result.copy(temp);
        } else {
            if (other.columns == 1) {
                gemv(1.0, this, other, 0.0, result);
            } else {
                gemm(1.0, this, other, 0.0, result);
            }
        }
        return result;
    }

    private void gemm(double alpha, DoubleMatrix matrix, DoubleMatrix other, double beta, DoubleMatrix result) {
        Matrix4d matrix1 = new Matrix4d();
        matrix1.set(matrix.data);

        Matrix4d matrix2 = new Matrix4d();
        matrix2.set(other.data);

        matrix1 = matrix1.mul(matrix2);
        matrix1.get(result.data);

        if (other.length == 32) {
            matrix1.set(matrix.data);

            matrix2 = new Matrix4d();
            matrix2.set(other.data, 16);

            matrix1 = matrix1.mul(matrix2);
            matrix1.get(result.data, 16);
        }
    }

    public DoubleMatrix dup() {
        DoubleMatrix out = new DoubleMatrix(rows, columns);
        out.copy(this);
        return out;
    }

    public static DoubleMatrix gemv(double alpha, DoubleMatrix a, DoubleMatrix x, double beta, DoubleMatrix y) {
        if (beta != 0.0) {
            for (int i = 0; i < y.length; i++) y.data[i] = beta * y.data[i];
        } else {
            for (int i = 0; i < y.length; i++) y.data[i] = 0.0;
        }

        for (int j = 0; j < a.columns; j++) {
            double xj = x.get(j);
            if (xj != 0.0) {
                for (int i = 0; i < a.rows; i++) y.data[i] += alpha * a.get(i, j) * xj;
            }
        }
        return y;
    }

}
