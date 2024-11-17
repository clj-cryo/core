(ns clj.cryo.kv-store.lmdb
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [java.nio ByteBuffer]
           [org.lmdbjava ByteBufferProxy Dbi DbiFlags Env EnvFlags]
           ))
;; In order for LMDB Java to load int memory properly, the JAVA_OPTS variable needs the 
;; following values set:
;; --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED
;; These have already been set in order to start Calva on VSCode with the proper settings 
;; in core.code-workspace under jackInEnv
(comment
  (def path (io/file (str (System/getProperty "java.io.tmpdir") "/lmdb/")))
  (.mkdirs path)
  (prn "path data type: " (type path)) 
  (def one-gigabyte 1073741824)
  (def map-size one-gigabyte)
  (def env-flags (make-array EnvFlags 1))
  (prn "env-flags: " env-flags)
  (prn "env-flags count: " (count env-flags))
  (def dbs 3)
  ;; This tells LMDB options to use when creating the database.
  ;; Use reverse string keys.
  ;; Keys are strings to be compared in reverse order, from the end of the 
  ;; strings to the beginning. By default, keys are treated as strings and 
  ;; compared from beginning to end.
  ;; MDB_REVERSEKEY(0x02),
  ;;
  ;; Use sorted duplicates. 
  ;; Duplicate keys may be used in the database. Or, from another perspective,
  ;; keys may have multiple data items, stored in sorted order. By default keys
  ;; must be unique and may have only a single data item.
  ;; MDB_DUPSORT(0x04),
  ;; 
  ;; Numeric keys in native byte order: either unsigned int or size_t. The keys
  ;; must all be of the same size.
  ;; MDB_INTEGERKEY(0x08),
  ;;
  ;; With {@link #MDB_DUPSORT}, sorted dup items have fixed size.
  ;; This flag may only be used in combination with {@link #MDB_DUPSORT}. This
  ;; option tells the library that the data items for this database are all the
  ;; same size, which allows further optimizations in storage and retrieval.
  ;; When all data items are the same size, the {@link SeekOp#MDB_GET_MULTIPLE}
  ;; and {@link SeekOp#MDB_NEXT_MULTIPLE} cursor operations may be used to
  ;; retrieve multiple items at once.
  ;; MDB_DUPFIXED(0x10),
  ;;
  ;; With {@link #MDB_DUPSORT}, dups are {@link #MDB_INTEGERKEY}-style integers.
  ;; This option specifies that duplicate data items are binary integers,
  ;; similar to {@link #MDB_INTEGERKEY} keys.
  ;; MDB_INTEGERDUP(0x20),
  ;;
  ;; With {@link #MDB_DUPSORT}, use reverse string dups.
  ;; This option specifies that duplicate data items should be compared as
  ;; strings in reverse order.
  ;; MDB_REVERSEDUP(0x40),
  ;;
  ;; Create the named database if it doesn't exist.
  ;; This option is not allowed in a read-only transaction or a read-only
  ;; environment.
  ;; MDB_CREATE(0x4_0000);
  (def dbi-flags (into-array DbiFlags [DbiFlags/MDB_CREATE]))

  ;; This line will fail to evaluate if JAVA_OPTS have not been set as described above.
  (def byte-buf-proxy ByteBufferProxy/PROXY_SAFE)
  ;; LMDB always needs and Env. An Env owns a physical on-disk storage file.
  ;; One Env can store multiple databases (e.g. - sorted maps)
  (def env ^Env (-> (Env/create)
               ;; LMDB needs to know how large our DB may become. Over-estimating is OK
               ;; This setst the map size in bytes
               (.setMapSize map-size)
               ;; LMDB needs to know how many DBs (Dbi) we want to store in this Env
               (.setMaxDbs dbs)
               ;; Open the Env. The same path can be concurrently opened and used in 
               ;; different processes, but do not open the same path twice in the same 
               ;; process at the same time.
               (.open path env-flags)))
  (def customer-db "customers")
  ;; We need a Dbi for each DB. A Dbi roughly equates to a sorted map. The
  ;; MDB_CREATE flag causes the DB to be created if it doesn't already exist.
  (def db ^Dbi (.openDbi env customer-db dbi-flags))
  )