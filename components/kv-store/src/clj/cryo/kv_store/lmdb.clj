(ns clj.cryo.kv-store.lmdb
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [java.nio ByteBuffer]
           [org.lmdbjava ByteBufferProxy Env EnvFlags]
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
  ;; This line will fail to evaluate if JAVA_OPTS have not been set as described above.
  (def byte-buf-proxy ByteBufferProxy/PROXY_SAFE)
  ;; LMDB always needs and Env. An Env owns a physical on-disk storage file.
  ;; One Env can store multiple databases (e.g. - sorted maps)
  (def env (-> (Env/create)
               ;; LMDB needs to know how large our DB may become. Over-estimating is OK
               ;; This setst the map size in bytes
               (.setMapSize map-size)
               ;; LMDB needs to know how many DBs (Dbi) we want to store in this Env
               (.setMaxDbs 1)
               ;; Open the Env. The same path can be concurrently opened and used in 
               ;; different processes, but do not open the same path twice in the same 
               ;; process at the same time.
               (.open path env-flags)))
  )