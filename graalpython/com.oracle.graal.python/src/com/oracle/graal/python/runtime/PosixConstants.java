/*
 * Copyright (c) 2021, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.graal.python.runtime;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives;

public final class PosixConstants {

    // @formatter:off Generated code
    // start generated by gen_native_cfg.py
    public static final MandatoryBooleanConstant HAVE_FUTIMENS;
    public static final MandatoryBooleanConstant HAVE_UTIMENSAT;
    public static final MandatoryIntConstant FD_SETSIZE;
    public static final MandatoryIntConstant PATH_MAX;
    public static final MandatoryIntConstant L_ctermid;
    public static final MandatoryIntConstant AT_FDCWD;
    public static final MandatoryIntConstant SEEK_SET;
    public static final MandatoryIntConstant SEEK_CUR;
    public static final MandatoryIntConstant SEEK_END;
    public static final OptionalIntConstant SEEK_DATA;
    public static final OptionalIntConstant SEEK_HOLE;
    public static final MandatoryIntConstant O_ACCMODE;
    public static final MandatoryIntConstant O_RDONLY;
    public static final MandatoryIntConstant O_WRONLY;
    public static final MandatoryIntConstant O_RDWR;
    public static final MandatoryIntConstant O_CREAT;
    public static final MandatoryIntConstant O_EXCL;
    public static final MandatoryIntConstant O_TRUNC;
    public static final MandatoryIntConstant O_APPEND;
    public static final MandatoryIntConstant O_NONBLOCK;
    public static final MandatoryIntConstant O_NDELAY;
    public static final MandatoryIntConstant O_DSYNC;
    public static final MandatoryIntConstant O_CLOEXEC;
    public static final MandatoryIntConstant O_SYNC;
    public static final OptionalIntConstant O_DIRECT;
    public static final OptionalIntConstant O_RSYNC;
    public static final OptionalIntConstant O_TMPFILE;
    public static final MandatoryIntConstant S_IFMT;
    public static final MandatoryIntConstant S_IFSOCK;
    public static final MandatoryIntConstant S_IFLNK;
    public static final MandatoryIntConstant S_IFREG;
    public static final MandatoryIntConstant S_IFBLK;
    public static final MandatoryIntConstant S_IFDIR;
    public static final MandatoryIntConstant S_IFCHR;
    public static final MandatoryIntConstant S_IFIFO;
    public static final MandatoryIntConstant MAP_SHARED;
    public static final MandatoryIntConstant MAP_PRIVATE;
    public static final MandatoryIntConstant MAP_ANONYMOUS;
    public static final OptionalIntConstant MAP_DENYWRITE;
    public static final OptionalIntConstant MAP_EXECUTABLE;
    public static final MandatoryIntConstant PROT_NONE;
    public static final MandatoryIntConstant PROT_READ;
    public static final MandatoryIntConstant PROT_WRITE;
    public static final MandatoryIntConstant PROT_EXEC;
    public static final MandatoryIntConstant LOCK_SH;
    public static final MandatoryIntConstant LOCK_EX;
    public static final MandatoryIntConstant LOCK_NB;
    public static final MandatoryIntConstant LOCK_UN;
    public static final OptionalIntConstant F_RDLCK;
    public static final OptionalIntConstant F_WRLCK;
    public static final OptionalIntConstant F_UNLCK;
    public static final MandatoryIntConstant DT_UNKNOWN;
    public static final MandatoryIntConstant DT_FIFO;
    public static final MandatoryIntConstant DT_CHR;
    public static final MandatoryIntConstant DT_DIR;
    public static final MandatoryIntConstant DT_BLK;
    public static final MandatoryIntConstant DT_REG;
    public static final MandatoryIntConstant DT_LNK;
    public static final MandatoryIntConstant DT_SOCK;
    public static final MandatoryIntConstant DT_WHT;
    public static final MandatoryIntConstant WNOHANG;
    public static final MandatoryIntConstant WUNTRACED;
    public static final MandatoryIntConstant R_OK;
    public static final MandatoryIntConstant W_OK;
    public static final MandatoryIntConstant X_OK;
    public static final MandatoryIntConstant F_OK;

    public static final IntConstant[] openFlags;
    public static final IntConstant[] fileType;
    public static final IntConstant[] mmapFlags;
    public static final IntConstant[] mmapProtection;
    public static final IntConstant[] flockOperation;
    public static final IntConstant[] flockType;
    public static final IntConstant[] direntType;
    public static final IntConstant[] waitOptions;
    public static final IntConstant[] accessMode;

    static {
        Registry reg = Registry.create();
        HAVE_FUTIMENS = reg.createMandatoryBoolean("HAVE_FUTIMENS");
        HAVE_UTIMENSAT = reg.createMandatoryBoolean("HAVE_UTIMENSAT");
        FD_SETSIZE = reg.createMandatoryInt("FD_SETSIZE");
        PATH_MAX = reg.createMandatoryInt("PATH_MAX");
        L_ctermid = reg.createMandatoryInt("L_ctermid");
        AT_FDCWD = reg.createMandatoryInt("AT_FDCWD");
        SEEK_SET = reg.createMandatoryInt("SEEK_SET");
        SEEK_CUR = reg.createMandatoryInt("SEEK_CUR");
        SEEK_END = reg.createMandatoryInt("SEEK_END");
        SEEK_DATA = reg.createOptionalInt("SEEK_DATA");
        SEEK_HOLE = reg.createOptionalInt("SEEK_HOLE");
        O_ACCMODE = reg.createMandatoryInt("O_ACCMODE");
        O_RDONLY = reg.createMandatoryInt("O_RDONLY");
        O_WRONLY = reg.createMandatoryInt("O_WRONLY");
        O_RDWR = reg.createMandatoryInt("O_RDWR");
        O_CREAT = reg.createMandatoryInt("O_CREAT");
        O_EXCL = reg.createMandatoryInt("O_EXCL");
        O_TRUNC = reg.createMandatoryInt("O_TRUNC");
        O_APPEND = reg.createMandatoryInt("O_APPEND");
        O_NONBLOCK = reg.createMandatoryInt("O_NONBLOCK");
        O_NDELAY = reg.createMandatoryInt("O_NDELAY");
        O_DSYNC = reg.createMandatoryInt("O_DSYNC");
        O_CLOEXEC = reg.createMandatoryInt("O_CLOEXEC");
        O_SYNC = reg.createMandatoryInt("O_SYNC");
        O_DIRECT = reg.createOptionalInt("O_DIRECT");
        O_RSYNC = reg.createOptionalInt("O_RSYNC");
        O_TMPFILE = reg.createOptionalInt("O_TMPFILE");
        S_IFMT = reg.createMandatoryInt("S_IFMT");
        S_IFSOCK = reg.createMandatoryInt("S_IFSOCK");
        S_IFLNK = reg.createMandatoryInt("S_IFLNK");
        S_IFREG = reg.createMandatoryInt("S_IFREG");
        S_IFBLK = reg.createMandatoryInt("S_IFBLK");
        S_IFDIR = reg.createMandatoryInt("S_IFDIR");
        S_IFCHR = reg.createMandatoryInt("S_IFCHR");
        S_IFIFO = reg.createMandatoryInt("S_IFIFO");
        MAP_SHARED = reg.createMandatoryInt("MAP_SHARED");
        MAP_PRIVATE = reg.createMandatoryInt("MAP_PRIVATE");
        MAP_ANONYMOUS = reg.createMandatoryInt("MAP_ANONYMOUS");
        MAP_DENYWRITE = reg.createOptionalInt("MAP_DENYWRITE");
        MAP_EXECUTABLE = reg.createOptionalInt("MAP_EXECUTABLE");
        PROT_NONE = reg.createMandatoryInt("PROT_NONE");
        PROT_READ = reg.createMandatoryInt("PROT_READ");
        PROT_WRITE = reg.createMandatoryInt("PROT_WRITE");
        PROT_EXEC = reg.createMandatoryInt("PROT_EXEC");
        LOCK_SH = reg.createMandatoryInt("LOCK_SH");
        LOCK_EX = reg.createMandatoryInt("LOCK_EX");
        LOCK_NB = reg.createMandatoryInt("LOCK_NB");
        LOCK_UN = reg.createMandatoryInt("LOCK_UN");
        F_RDLCK = reg.createOptionalInt("F_RDLCK");
        F_WRLCK = reg.createOptionalInt("F_WRLCK");
        F_UNLCK = reg.createOptionalInt("F_UNLCK");
        DT_UNKNOWN = reg.createMandatoryInt("DT_UNKNOWN");
        DT_FIFO = reg.createMandatoryInt("DT_FIFO");
        DT_CHR = reg.createMandatoryInt("DT_CHR");
        DT_DIR = reg.createMandatoryInt("DT_DIR");
        DT_BLK = reg.createMandatoryInt("DT_BLK");
        DT_REG = reg.createMandatoryInt("DT_REG");
        DT_LNK = reg.createMandatoryInt("DT_LNK");
        DT_SOCK = reg.createMandatoryInt("DT_SOCK");
        DT_WHT = reg.createMandatoryInt("DT_WHT");
        WNOHANG = reg.createMandatoryInt("WNOHANG");
        WUNTRACED = reg.createMandatoryInt("WUNTRACED");
        R_OK = reg.createMandatoryInt("R_OK");
        W_OK = reg.createMandatoryInt("W_OK");
        X_OK = reg.createMandatoryInt("X_OK");
        F_OK = reg.createMandatoryInt("F_OK");

        openFlags = new IntConstant[]{O_ACCMODE, O_RDONLY, O_WRONLY, O_RDWR, O_CREAT, O_EXCL, O_TRUNC, O_APPEND, O_NONBLOCK, O_NDELAY, O_DSYNC, O_CLOEXEC, O_SYNC, O_DIRECT, O_RSYNC, O_TMPFILE};
        fileType = new IntConstant[]{S_IFMT, S_IFSOCK, S_IFLNK, S_IFREG, S_IFBLK, S_IFDIR, S_IFCHR, S_IFIFO};
        mmapFlags = new IntConstant[]{MAP_SHARED, MAP_PRIVATE, MAP_ANONYMOUS, MAP_DENYWRITE, MAP_EXECUTABLE};
        mmapProtection = new IntConstant[]{PROT_NONE, PROT_READ, PROT_WRITE, PROT_EXEC};
        flockOperation = new IntConstant[]{LOCK_SH, LOCK_EX, LOCK_NB, LOCK_UN};
        flockType = new IntConstant[]{F_RDLCK, F_WRLCK, F_UNLCK};
        direntType = new IntConstant[]{DT_UNKNOWN, DT_FIFO, DT_CHR, DT_DIR, DT_BLK, DT_REG, DT_LNK, DT_SOCK, DT_WHT};
        waitOptions = new IntConstant[]{WNOHANG, WUNTRACED};
        accessMode = new IntConstant[]{R_OK, W_OK, X_OK, F_OK};
    }
    // end generated by gen_native_cfg.py
    // @formatter:on

    public abstract static class IntConstant {
        public final String name;
        public final boolean defined;

        protected IntConstant(String name, boolean defined) {
            this.name = name;
            this.defined = defined;
        }

        public abstract int getValueIfDefined();
    }

    public static final class MandatoryIntConstant extends IntConstant {
        public final int value;

        public MandatoryIntConstant(String name, int value) {
            super(name, true);
            this.value = value;
        }

        @Override
        public int getValueIfDefined() {
            return value;
        }
    }

    public static final class OptionalIntConstant extends IntConstant {
        private final int value;

        public OptionalIntConstant(String name) {
            super(name, false);
            this.value = 0;
        }

        public OptionalIntConstant(String name, int value) {
            super(name, true);
            this.value = value;
        }

        @Override
        public int getValueIfDefined() {
            if (!defined) {
                throw CompilerDirectives.shouldNotReachHere();
            }
            return value;
        }
    }

    public abstract static class BooleanConstant {
        public final String name;
        public final boolean defined;

        protected BooleanConstant(String name, boolean defined) {
            this.name = name;
            this.defined = defined;
        }

        public abstract boolean getValueIfDefined();
    }

    public static final class MandatoryBooleanConstant extends BooleanConstant {
        public final boolean value;

        public MandatoryBooleanConstant(String name, boolean value) {
            super(name, true);
            this.value = value;
        }

        @Override
        public boolean getValueIfDefined() {
            return value;
        }
    }

    static final class Registry {
        private final Map<String, Object> constants = new HashMap<>();

        private Registry() {
        }

        static Registry create() {
            Registry registry = new Registry();
            String os = System.getProperty("os.name");
            if (os.contains("Linux")) {
                PosixConstantsLinux.getConstants(registry);
            } else if (os.contains("Mac")) {
                PosixConstantsDarwin.getConstants(registry);
            } else {
                throw new RuntimeException("Unsupported platform " + os);
            }
            return registry;
        }

        void put(String name, Object value) {
            constants.put(name, value);
        }

        MandatoryIntConstant createMandatoryInt(String name) {
            Object value = constants.get(name);
            return new MandatoryIntConstant(name, (int) value);
        }

        OptionalIntConstant createOptionalInt(String name) {
            Object value = constants.get(name);
            return value == null ? new OptionalIntConstant(name) : new OptionalIntConstant(name, (int) value);
        }

        MandatoryBooleanConstant createMandatoryBoolean(String name) {
            Object value = constants.get(name);
            return new MandatoryBooleanConstant(name, (boolean) value);
        }
    }
}