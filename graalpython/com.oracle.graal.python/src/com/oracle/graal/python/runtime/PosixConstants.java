/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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

    public static final byte[] IN6ADDR_ANY = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final byte[] IN6ADDR_LOOPBACK = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

    // formatter:off Generated code
    // start generated by gen_native_cfg.py
    public static final MandatoryBooleanConstant HAVE_FUTIMENS;
    public static final MandatoryBooleanConstant HAVE_UTIMENSAT;
    public static final MandatoryIntConstant FD_SETSIZE;
    public static final MandatoryIntConstant PATH_MAX;
    public static final MandatoryIntConstant L_ctermid;
    public static final MandatoryIntConstant INET_ADDRSTRLEN;
    public static final MandatoryIntConstant INET6_ADDRSTRLEN;
    public static final OptionalIntConstant HOST_NAME_MAX;
    public static final MandatoryIntConstant _POSIX_HOST_NAME_MAX;
    public static final MandatoryIntConstant SOL_SOCKET;
    public static final MandatoryIntConstant NI_MAXHOST;
    public static final MandatoryIntConstant NI_MAXSERV;
    public static final MandatoryIntConstant AT_FDCWD;
    public static final MandatoryIntConstant SEEK_SET;
    public static final MandatoryIntConstant SEEK_CUR;
    public static final MandatoryIntConstant SEEK_END;
    public static final OptionalIntConstant SEEK_DATA;
    public static final OptionalIntConstant SEEK_HOLE;
    public static final MandatoryIntConstant SOMAXCONN;
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
    public static final MandatoryIntConstant EX_OK;
    public static final MandatoryIntConstant RTLD_LAZY;
    public static final MandatoryIntConstant RTLD_NOW;
    public static final MandatoryIntConstant RTLD_GLOBAL;
    public static final MandatoryIntConstant RTLD_LOCAL;
    public static final MandatoryIntConstant AF_UNSPEC;
    public static final MandatoryIntConstant AF_INET;
    public static final MandatoryIntConstant AF_INET6;
    public static final OptionalIntConstant AF_PACKET;
    public static final MandatoryIntConstant AF_UNIX;
    public static final MandatoryIntConstant SOCK_DGRAM;
    public static final MandatoryIntConstant SOCK_STREAM;
    public static final MandatoryIntConstant INADDR_ANY;
    public static final MandatoryIntConstant INADDR_BROADCAST;
    public static final MandatoryIntConstant INADDR_NONE;
    public static final MandatoryIntConstant INADDR_LOOPBACK;
    public static final MandatoryIntConstant INADDR_ALLHOSTS_GROUP;
    public static final MandatoryIntConstant INADDR_MAX_LOCAL_GROUP;
    public static final MandatoryIntConstant INADDR_UNSPEC_GROUP;
    public static final MandatoryIntConstant AI_PASSIVE;
    public static final MandatoryIntConstant AI_CANONNAME;
    public static final MandatoryIntConstant AI_NUMERICHOST;
    public static final MandatoryIntConstant AI_V4MAPPED;
    public static final MandatoryIntConstant AI_ALL;
    public static final MandatoryIntConstant AI_ADDRCONFIG;
    public static final OptionalIntConstant AI_IDN;
    public static final OptionalIntConstant AI_CANONIDN;
    public static final MandatoryIntConstant AI_NUMERICSERV;
    public static final MandatoryIntConstant EAI_BADFLAGS;
    public static final MandatoryIntConstant EAI_NONAME;
    public static final MandatoryIntConstant EAI_AGAIN;
    public static final MandatoryIntConstant EAI_FAIL;
    public static final MandatoryIntConstant EAI_FAMILY;
    public static final MandatoryIntConstant EAI_SOCKTYPE;
    public static final MandatoryIntConstant EAI_SERVICE;
    public static final MandatoryIntConstant EAI_MEMORY;
    public static final MandatoryIntConstant EAI_SYSTEM;
    public static final MandatoryIntConstant EAI_OVERFLOW;
    public static final MandatoryIntConstant EAI_NODATA;
    public static final MandatoryIntConstant EAI_ADDRFAMILY;
    public static final OptionalIntConstant EAI_INPROGRESS;
    public static final OptionalIntConstant EAI_CANCELED;
    public static final OptionalIntConstant EAI_NOTCANCELED;
    public static final OptionalIntConstant EAI_ALLDONE;
    public static final OptionalIntConstant EAI_INTR;
    public static final OptionalIntConstant EAI_IDN_ENCODE;
    public static final MandatoryIntConstant NI_NUMERICHOST;
    public static final MandatoryIntConstant NI_NUMERICSERV;
    public static final MandatoryIntConstant NI_NOFQDN;
    public static final MandatoryIntConstant NI_NAMEREQD;
    public static final MandatoryIntConstant NI_DGRAM;
    public static final OptionalIntConstant NI_IDN;
    public static final MandatoryIntConstant IPPROTO_IP;
    public static final MandatoryIntConstant IPPROTO_ICMP;
    public static final MandatoryIntConstant IPPROTO_IGMP;
    public static final MandatoryIntConstant IPPROTO_IPIP;
    public static final MandatoryIntConstant IPPROTO_TCP;
    public static final MandatoryIntConstant IPPROTO_EGP;
    public static final MandatoryIntConstant IPPROTO_PUP;
    public static final MandatoryIntConstant IPPROTO_UDP;
    public static final MandatoryIntConstant IPPROTO_IDP;
    public static final MandatoryIntConstant IPPROTO_TP;
    public static final MandatoryIntConstant IPPROTO_IPV6;
    public static final MandatoryIntConstant IPPROTO_RSVP;
    public static final MandatoryIntConstant IPPROTO_GRE;
    public static final MandatoryIntConstant IPPROTO_ESP;
    public static final MandatoryIntConstant IPPROTO_AH;
    public static final MandatoryIntConstant IPPROTO_MTP;
    public static final MandatoryIntConstant IPPROTO_ENCAP;
    public static final MandatoryIntConstant IPPROTO_PIM;
    public static final MandatoryIntConstant IPPROTO_SCTP;
    public static final MandatoryIntConstant IPPROTO_RAW;
    public static final MandatoryIntConstant SHUT_RD;
    public static final MandatoryIntConstant SHUT_WR;
    public static final MandatoryIntConstant SHUT_RDWR;
    public static final MandatoryIntConstant SO_DEBUG;
    public static final MandatoryIntConstant SO_ACCEPTCONN;
    public static final MandatoryIntConstant SO_REUSEADDR;
    public static final OptionalIntConstant SO_EXCLUSIVEADDRUSE;
    public static final MandatoryIntConstant SO_KEEPALIVE;
    public static final MandatoryIntConstant SO_DONTROUTE;
    public static final MandatoryIntConstant SO_BROADCAST;
    public static final OptionalIntConstant SO_USELOOPBACK;
    public static final MandatoryIntConstant SO_LINGER;
    public static final MandatoryIntConstant SO_OOBINLINE;
    public static final MandatoryIntConstant SO_REUSEPORT;
    public static final MandatoryIntConstant SO_SNDBUF;
    public static final MandatoryIntConstant SO_RCVBUF;
    public static final MandatoryIntConstant SO_SNDLOWAT;
    public static final MandatoryIntConstant SO_RCVLOWAT;
    public static final MandatoryIntConstant SO_SNDTIMEO;
    public static final MandatoryIntConstant SO_RCVTIMEO;
    public static final MandatoryIntConstant SO_ERROR;
    public static final MandatoryIntConstant SO_TYPE;
    public static final OptionalIntConstant SO_SETFIB;
    public static final OptionalIntConstant SO_PASSCRED;
    public static final OptionalIntConstant SO_PEERCRED;
    public static final OptionalIntConstant SO_PASSSEC;
    public static final OptionalIntConstant SO_PEERSEC;
    public static final OptionalIntConstant SO_BINDTODEVICE;
    public static final OptionalIntConstant SO_PRIORITY;
    public static final OptionalIntConstant SO_MARK;
    public static final OptionalIntConstant SO_DOMAIN;
    public static final OptionalIntConstant SO_PROTOCOL;
    public static final OptionalIntConstant TCP_NODELAY;
    public static final OptionalIntConstant TCP_MAXSEG;
    public static final OptionalIntConstant TCP_CORK;
    public static final OptionalIntConstant TCP_KEEPIDLE;
    public static final OptionalIntConstant TCP_KEEPINTVL;
    public static final OptionalIntConstant TCP_KEEPCNT;
    public static final OptionalIntConstant TCP_SYNCNT;
    public static final OptionalIntConstant TCP_LINGER2;
    public static final OptionalIntConstant TCP_DEFER_ACCEPT;
    public static final OptionalIntConstant TCP_WINDOW_CLAMP;
    public static final OptionalIntConstant TCP_INFO;
    public static final OptionalIntConstant TCP_QUICKACK;
    public static final OptionalIntConstant TCP_FASTOPEN;
    public static final OptionalIntConstant TCP_CONGESTION;
    public static final OptionalIntConstant TCP_USER_TIMEOUT;
    public static final OptionalIntConstant TCP_NOTSENT_LOWAT;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_STORAGE;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_IN;
    public static final MandatoryIntConstant OFFSETOF_STRUCT_SOCKADDR_IN_SIN_FAMILY;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_IN_SIN_FAMILY;
    public static final MandatoryIntConstant OFFSETOF_STRUCT_SOCKADDR_IN_SIN_PORT;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_IN_SIN_PORT;
    public static final MandatoryIntConstant OFFSETOF_STRUCT_SOCKADDR_IN_SIN_ADDR;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_IN_SIN_ADDR;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_IN6;
    public static final MandatoryIntConstant SIZEOF_STRUCT_IN_ADDR;
    public static final MandatoryIntConstant OFFSETOF_STRUCT_IN_ADDR_S_ADDR;
    public static final MandatoryIntConstant SIZEOF_STRUCT_IN_ADDR_S_ADDR;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_UN;
    public static final MandatoryIntConstant OFFSETOF_STRUCT_SOCKADDR_UN_SUN_PATH;
    public static final MandatoryIntConstant SIZEOF_STRUCT_SOCKADDR_UN_SUN_PATH;

    public static final IntConstant[] openFlags;
    public static final IntConstant[] fileType;
    public static final IntConstant[] mmapFlags;
    public static final IntConstant[] mmapProtection;
    public static final IntConstant[] flockOperation;
    public static final IntConstant[] flockType;
    public static final IntConstant[] direntType;
    public static final IntConstant[] waitOptions;
    public static final IntConstant[] accessMode;
    public static final IntConstant[] rtld;
    public static final IntConstant[] socketFamily;
    public static final IntConstant[] socketType;
    public static final IntConstant[] ip4Address;
    public static final IntConstant[] gaiFlags;
    public static final IntConstant[] gaiErrors;
    public static final IntConstant[] niFlags;
    public static final IntConstant[] ipProto;
    public static final IntConstant[] shutdownHow;
    public static final IntConstant[] socketOptions;
    public static final IntConstant[] tcpOptions;

    static {
        Registry reg = Registry.create();
        HAVE_FUTIMENS = reg.createMandatoryBoolean("HAVE_FUTIMENS");
        HAVE_UTIMENSAT = reg.createMandatoryBoolean("HAVE_UTIMENSAT");
        FD_SETSIZE = reg.createMandatoryInt("FD_SETSIZE");
        PATH_MAX = reg.createMandatoryInt("PATH_MAX");
        L_ctermid = reg.createMandatoryInt("L_ctermid");
        INET_ADDRSTRLEN = reg.createMandatoryInt("INET_ADDRSTRLEN");
        INET6_ADDRSTRLEN = reg.createMandatoryInt("INET6_ADDRSTRLEN");
        HOST_NAME_MAX = reg.createOptionalInt("HOST_NAME_MAX");
        _POSIX_HOST_NAME_MAX = reg.createMandatoryInt("_POSIX_HOST_NAME_MAX");
        SOL_SOCKET = reg.createMandatoryInt("SOL_SOCKET");
        NI_MAXHOST = reg.createMandatoryInt("NI_MAXHOST");
        NI_MAXSERV = reg.createMandatoryInt("NI_MAXSERV");
        AT_FDCWD = reg.createMandatoryInt("AT_FDCWD");
        SEEK_SET = reg.createMandatoryInt("SEEK_SET");
        SEEK_CUR = reg.createMandatoryInt("SEEK_CUR");
        SEEK_END = reg.createMandatoryInt("SEEK_END");
        SEEK_DATA = reg.createOptionalInt("SEEK_DATA");
        SEEK_HOLE = reg.createOptionalInt("SEEK_HOLE");
        SOMAXCONN = reg.createMandatoryInt("SOMAXCONN");
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
        EX_OK = reg.createMandatoryInt("EX_OK");
        RTLD_LAZY = reg.createMandatoryInt("RTLD_LAZY");
        RTLD_NOW = reg.createMandatoryInt("RTLD_NOW");
        RTLD_GLOBAL = reg.createMandatoryInt("RTLD_GLOBAL");
        RTLD_LOCAL = reg.createMandatoryInt("RTLD_LOCAL");
        AF_UNSPEC = reg.createMandatoryInt("AF_UNSPEC");
        AF_INET = reg.createMandatoryInt("AF_INET");
        AF_INET6 = reg.createMandatoryInt("AF_INET6");
        AF_PACKET = reg.createOptionalInt("AF_PACKET");
        AF_UNIX = reg.createMandatoryInt("AF_UNIX");
        SOCK_DGRAM = reg.createMandatoryInt("SOCK_DGRAM");
        SOCK_STREAM = reg.createMandatoryInt("SOCK_STREAM");
        INADDR_ANY = reg.createMandatoryInt("INADDR_ANY");
        INADDR_BROADCAST = reg.createMandatoryInt("INADDR_BROADCAST");
        INADDR_NONE = reg.createMandatoryInt("INADDR_NONE");
        INADDR_LOOPBACK = reg.createMandatoryInt("INADDR_LOOPBACK");
        INADDR_ALLHOSTS_GROUP = reg.createMandatoryInt("INADDR_ALLHOSTS_GROUP");
        INADDR_MAX_LOCAL_GROUP = reg.createMandatoryInt("INADDR_MAX_LOCAL_GROUP");
        INADDR_UNSPEC_GROUP = reg.createMandatoryInt("INADDR_UNSPEC_GROUP");
        AI_PASSIVE = reg.createMandatoryInt("AI_PASSIVE");
        AI_CANONNAME = reg.createMandatoryInt("AI_CANONNAME");
        AI_NUMERICHOST = reg.createMandatoryInt("AI_NUMERICHOST");
        AI_V4MAPPED = reg.createMandatoryInt("AI_V4MAPPED");
        AI_ALL = reg.createMandatoryInt("AI_ALL");
        AI_ADDRCONFIG = reg.createMandatoryInt("AI_ADDRCONFIG");
        AI_IDN = reg.createOptionalInt("AI_IDN");
        AI_CANONIDN = reg.createOptionalInt("AI_CANONIDN");
        AI_NUMERICSERV = reg.createMandatoryInt("AI_NUMERICSERV");
        EAI_BADFLAGS = reg.createMandatoryInt("EAI_BADFLAGS");
        EAI_NONAME = reg.createMandatoryInt("EAI_NONAME");
        EAI_AGAIN = reg.createMandatoryInt("EAI_AGAIN");
        EAI_FAIL = reg.createMandatoryInt("EAI_FAIL");
        EAI_FAMILY = reg.createMandatoryInt("EAI_FAMILY");
        EAI_SOCKTYPE = reg.createMandatoryInt("EAI_SOCKTYPE");
        EAI_SERVICE = reg.createMandatoryInt("EAI_SERVICE");
        EAI_MEMORY = reg.createMandatoryInt("EAI_MEMORY");
        EAI_SYSTEM = reg.createMandatoryInt("EAI_SYSTEM");
        EAI_OVERFLOW = reg.createMandatoryInt("EAI_OVERFLOW");
        EAI_NODATA = reg.createMandatoryInt("EAI_NODATA");
        EAI_ADDRFAMILY = reg.createMandatoryInt("EAI_ADDRFAMILY");
        EAI_INPROGRESS = reg.createOptionalInt("EAI_INPROGRESS");
        EAI_CANCELED = reg.createOptionalInt("EAI_CANCELED");
        EAI_NOTCANCELED = reg.createOptionalInt("EAI_NOTCANCELED");
        EAI_ALLDONE = reg.createOptionalInt("EAI_ALLDONE");
        EAI_INTR = reg.createOptionalInt("EAI_INTR");
        EAI_IDN_ENCODE = reg.createOptionalInt("EAI_IDN_ENCODE");
        NI_NUMERICHOST = reg.createMandatoryInt("NI_NUMERICHOST");
        NI_NUMERICSERV = reg.createMandatoryInt("NI_NUMERICSERV");
        NI_NOFQDN = reg.createMandatoryInt("NI_NOFQDN");
        NI_NAMEREQD = reg.createMandatoryInt("NI_NAMEREQD");
        NI_DGRAM = reg.createMandatoryInt("NI_DGRAM");
        NI_IDN = reg.createOptionalInt("NI_IDN");
        IPPROTO_IP = reg.createMandatoryInt("IPPROTO_IP");
        IPPROTO_ICMP = reg.createMandatoryInt("IPPROTO_ICMP");
        IPPROTO_IGMP = reg.createMandatoryInt("IPPROTO_IGMP");
        IPPROTO_IPIP = reg.createMandatoryInt("IPPROTO_IPIP");
        IPPROTO_TCP = reg.createMandatoryInt("IPPROTO_TCP");
        IPPROTO_EGP = reg.createMandatoryInt("IPPROTO_EGP");
        IPPROTO_PUP = reg.createMandatoryInt("IPPROTO_PUP");
        IPPROTO_UDP = reg.createMandatoryInt("IPPROTO_UDP");
        IPPROTO_IDP = reg.createMandatoryInt("IPPROTO_IDP");
        IPPROTO_TP = reg.createMandatoryInt("IPPROTO_TP");
        IPPROTO_IPV6 = reg.createMandatoryInt("IPPROTO_IPV6");
        IPPROTO_RSVP = reg.createMandatoryInt("IPPROTO_RSVP");
        IPPROTO_GRE = reg.createMandatoryInt("IPPROTO_GRE");
        IPPROTO_ESP = reg.createMandatoryInt("IPPROTO_ESP");
        IPPROTO_AH = reg.createMandatoryInt("IPPROTO_AH");
        IPPROTO_MTP = reg.createMandatoryInt("IPPROTO_MTP");
        IPPROTO_ENCAP = reg.createMandatoryInt("IPPROTO_ENCAP");
        IPPROTO_PIM = reg.createMandatoryInt("IPPROTO_PIM");
        IPPROTO_SCTP = reg.createMandatoryInt("IPPROTO_SCTP");
        IPPROTO_RAW = reg.createMandatoryInt("IPPROTO_RAW");
        SHUT_RD = reg.createMandatoryInt("SHUT_RD");
        SHUT_WR = reg.createMandatoryInt("SHUT_WR");
        SHUT_RDWR = reg.createMandatoryInt("SHUT_RDWR");
        SO_DEBUG = reg.createMandatoryInt("SO_DEBUG");
        SO_ACCEPTCONN = reg.createMandatoryInt("SO_ACCEPTCONN");
        SO_REUSEADDR = reg.createMandatoryInt("SO_REUSEADDR");
        SO_EXCLUSIVEADDRUSE = reg.createOptionalInt("SO_EXCLUSIVEADDRUSE");
        SO_KEEPALIVE = reg.createMandatoryInt("SO_KEEPALIVE");
        SO_DONTROUTE = reg.createMandatoryInt("SO_DONTROUTE");
        SO_BROADCAST = reg.createMandatoryInt("SO_BROADCAST");
        SO_USELOOPBACK = reg.createOptionalInt("SO_USELOOPBACK");
        SO_LINGER = reg.createMandatoryInt("SO_LINGER");
        SO_OOBINLINE = reg.createMandatoryInt("SO_OOBINLINE");
        SO_REUSEPORT = reg.createMandatoryInt("SO_REUSEPORT");
        SO_SNDBUF = reg.createMandatoryInt("SO_SNDBUF");
        SO_RCVBUF = reg.createMandatoryInt("SO_RCVBUF");
        SO_SNDLOWAT = reg.createMandatoryInt("SO_SNDLOWAT");
        SO_RCVLOWAT = reg.createMandatoryInt("SO_RCVLOWAT");
        SO_SNDTIMEO = reg.createMandatoryInt("SO_SNDTIMEO");
        SO_RCVTIMEO = reg.createMandatoryInt("SO_RCVTIMEO");
        SO_ERROR = reg.createMandatoryInt("SO_ERROR");
        SO_TYPE = reg.createMandatoryInt("SO_TYPE");
        SO_SETFIB = reg.createOptionalInt("SO_SETFIB");
        SO_PASSCRED = reg.createOptionalInt("SO_PASSCRED");
        SO_PEERCRED = reg.createOptionalInt("SO_PEERCRED");
        SO_PASSSEC = reg.createOptionalInt("SO_PASSSEC");
        SO_PEERSEC = reg.createOptionalInt("SO_PEERSEC");
        SO_BINDTODEVICE = reg.createOptionalInt("SO_BINDTODEVICE");
        SO_PRIORITY = reg.createOptionalInt("SO_PRIORITY");
        SO_MARK = reg.createOptionalInt("SO_MARK");
        SO_DOMAIN = reg.createOptionalInt("SO_DOMAIN");
        SO_PROTOCOL = reg.createOptionalInt("SO_PROTOCOL");
        TCP_NODELAY = reg.createOptionalInt("TCP_NODELAY");
        TCP_MAXSEG = reg.createOptionalInt("TCP_MAXSEG");
        TCP_CORK = reg.createOptionalInt("TCP_CORK");
        TCP_KEEPIDLE = reg.createOptionalInt("TCP_KEEPIDLE");
        TCP_KEEPINTVL = reg.createOptionalInt("TCP_KEEPINTVL");
        TCP_KEEPCNT = reg.createOptionalInt("TCP_KEEPCNT");
        TCP_SYNCNT = reg.createOptionalInt("TCP_SYNCNT");
        TCP_LINGER2 = reg.createOptionalInt("TCP_LINGER2");
        TCP_DEFER_ACCEPT = reg.createOptionalInt("TCP_DEFER_ACCEPT");
        TCP_WINDOW_CLAMP = reg.createOptionalInt("TCP_WINDOW_CLAMP");
        TCP_INFO = reg.createOptionalInt("TCP_INFO");
        TCP_QUICKACK = reg.createOptionalInt("TCP_QUICKACK");
        TCP_FASTOPEN = reg.createOptionalInt("TCP_FASTOPEN");
        TCP_CONGESTION = reg.createOptionalInt("TCP_CONGESTION");
        TCP_USER_TIMEOUT = reg.createOptionalInt("TCP_USER_TIMEOUT");
        TCP_NOTSENT_LOWAT = reg.createOptionalInt("TCP_NOTSENT_LOWAT");
        SIZEOF_STRUCT_SOCKADDR_STORAGE = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_STORAGE");
        SIZEOF_STRUCT_SOCKADDR_IN = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_IN");
        OFFSETOF_STRUCT_SOCKADDR_IN_SIN_FAMILY = reg.createMandatoryInt("OFFSETOF_STRUCT_SOCKADDR_IN_SIN_FAMILY");
        SIZEOF_STRUCT_SOCKADDR_IN_SIN_FAMILY = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_IN_SIN_FAMILY");
        OFFSETOF_STRUCT_SOCKADDR_IN_SIN_PORT = reg.createMandatoryInt("OFFSETOF_STRUCT_SOCKADDR_IN_SIN_PORT");
        SIZEOF_STRUCT_SOCKADDR_IN_SIN_PORT = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_IN_SIN_PORT");
        OFFSETOF_STRUCT_SOCKADDR_IN_SIN_ADDR = reg.createMandatoryInt("OFFSETOF_STRUCT_SOCKADDR_IN_SIN_ADDR");
        SIZEOF_STRUCT_SOCKADDR_IN_SIN_ADDR = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_IN_SIN_ADDR");
        SIZEOF_STRUCT_SOCKADDR_IN6 = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_IN6");
        SIZEOF_STRUCT_IN_ADDR = reg.createMandatoryInt("SIZEOF_STRUCT_IN_ADDR");
        OFFSETOF_STRUCT_IN_ADDR_S_ADDR = reg.createMandatoryInt("OFFSETOF_STRUCT_IN_ADDR_S_ADDR");
        SIZEOF_STRUCT_IN_ADDR_S_ADDR = reg.createMandatoryInt("SIZEOF_STRUCT_IN_ADDR_S_ADDR");
        SIZEOF_STRUCT_SOCKADDR_UN = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_UN");
        OFFSETOF_STRUCT_SOCKADDR_UN_SUN_PATH = reg.createMandatoryInt("OFFSETOF_STRUCT_SOCKADDR_UN_SUN_PATH");
        SIZEOF_STRUCT_SOCKADDR_UN_SUN_PATH = reg.createMandatoryInt("SIZEOF_STRUCT_SOCKADDR_UN_SUN_PATH");

        openFlags = new IntConstant[]{O_ACCMODE, O_RDONLY, O_WRONLY, O_RDWR, O_CREAT, O_EXCL, O_TRUNC, O_APPEND, O_NONBLOCK, O_NDELAY, O_DSYNC, O_CLOEXEC, O_SYNC, O_DIRECT, O_RSYNC, O_TMPFILE};
        fileType = new IntConstant[]{S_IFMT, S_IFSOCK, S_IFLNK, S_IFREG, S_IFBLK, S_IFDIR, S_IFCHR, S_IFIFO};
        mmapFlags = new IntConstant[]{MAP_SHARED, MAP_PRIVATE, MAP_ANONYMOUS, MAP_DENYWRITE, MAP_EXECUTABLE};
        mmapProtection = new IntConstant[]{PROT_NONE, PROT_READ, PROT_WRITE, PROT_EXEC};
        flockOperation = new IntConstant[]{LOCK_SH, LOCK_EX, LOCK_NB, LOCK_UN};
        flockType = new IntConstant[]{F_RDLCK, F_WRLCK, F_UNLCK};
        direntType = new IntConstant[]{DT_UNKNOWN, DT_FIFO, DT_CHR, DT_DIR, DT_BLK, DT_REG, DT_LNK, DT_SOCK, DT_WHT};
        waitOptions = new IntConstant[]{WNOHANG, WUNTRACED};
        accessMode = new IntConstant[]{R_OK, W_OK, X_OK, F_OK, EX_OK};
        rtld = new IntConstant[]{RTLD_LAZY, RTLD_NOW, RTLD_GLOBAL, RTLD_LOCAL};
        socketFamily = new IntConstant[]{AF_UNSPEC, AF_INET, AF_INET6, AF_PACKET, AF_UNIX};
        socketType = new IntConstant[]{SOCK_DGRAM, SOCK_STREAM};
        ip4Address = new IntConstant[]{INADDR_ANY, INADDR_BROADCAST, INADDR_NONE, INADDR_LOOPBACK, INADDR_ALLHOSTS_GROUP, INADDR_MAX_LOCAL_GROUP, INADDR_UNSPEC_GROUP};
        gaiFlags = new IntConstant[]{AI_PASSIVE, AI_CANONNAME, AI_NUMERICHOST, AI_V4MAPPED, AI_ALL, AI_ADDRCONFIG, AI_IDN, AI_CANONIDN, AI_NUMERICSERV};
        gaiErrors = new IntConstant[]{EAI_BADFLAGS, EAI_NONAME, EAI_AGAIN, EAI_FAIL, EAI_FAMILY, EAI_SOCKTYPE, EAI_SERVICE, EAI_MEMORY, EAI_SYSTEM, EAI_OVERFLOW, EAI_NODATA, EAI_ADDRFAMILY,
                        EAI_INPROGRESS, EAI_CANCELED, EAI_NOTCANCELED, EAI_ALLDONE, EAI_INTR, EAI_IDN_ENCODE};
        niFlags = new IntConstant[]{NI_NUMERICHOST, NI_NUMERICSERV, NI_NOFQDN, NI_NAMEREQD, NI_DGRAM, NI_IDN};
        ipProto = new IntConstant[]{IPPROTO_IP, IPPROTO_ICMP, IPPROTO_IGMP, IPPROTO_IPIP, IPPROTO_TCP, IPPROTO_EGP, IPPROTO_PUP, IPPROTO_UDP, IPPROTO_IDP, IPPROTO_TP, IPPROTO_IPV6, IPPROTO_RSVP,
                        IPPROTO_GRE, IPPROTO_ESP, IPPROTO_AH, IPPROTO_MTP, IPPROTO_ENCAP, IPPROTO_PIM, IPPROTO_SCTP, IPPROTO_RAW};
        shutdownHow = new IntConstant[]{SHUT_RD, SHUT_WR, SHUT_RDWR};
        socketOptions = new IntConstant[]{SO_DEBUG, SO_ACCEPTCONN, SO_REUSEADDR, SO_EXCLUSIVEADDRUSE, SO_KEEPALIVE, SO_DONTROUTE, SO_BROADCAST, SO_USELOOPBACK, SO_LINGER, SO_OOBINLINE, SO_REUSEPORT,
                        SO_SNDBUF, SO_RCVBUF, SO_SNDLOWAT, SO_RCVLOWAT, SO_SNDTIMEO, SO_RCVTIMEO, SO_ERROR, SO_TYPE, SO_SETFIB, SO_PASSCRED, SO_PEERCRED, SO_PASSSEC, SO_PEERSEC, SO_BINDTODEVICE,
                        SO_PRIORITY, SO_MARK, SO_DOMAIN, SO_PROTOCOL};
        tcpOptions = new IntConstant[]{TCP_NODELAY, TCP_MAXSEG, TCP_CORK, TCP_KEEPIDLE, TCP_KEEPINTVL, TCP_KEEPCNT, TCP_SYNCNT, TCP_LINGER2, TCP_DEFER_ACCEPT, TCP_WINDOW_CLAMP, TCP_INFO, TCP_QUICKACK,
                        TCP_FASTOPEN, TCP_CONGESTION, TCP_USER_TIMEOUT, TCP_NOTSENT_LOWAT};
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
