(ns provisdom.deps-jupyter.kernel
  (:require [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [clojure.string :refer [lower-case includes? starts-with?]]))

(def python-kernel-script (-> "leinclojure.py" io/resource slurp))

(defn get-os []
  (let [os-name (-> "os.name" System/getProperty clojure.string/lower-case)]
    (cond
      (includes? os-name "mac") :mac
      (includes? os-name "win") :windows
      (includes? os-name "linux") :linux)))

(defn get-platform-specific-kernel-dir
  "Returns the directory where the kernel should be installed given the os"
  [os environ]
  (let [home (get environ "HOME")
        appdata (get environ "APPDATA")]
    (case os
      :mac (io/file home "Library/Jupyter/kernels/")
      :linux (io/file home ".local/share/jupyter/kernels")
      :windows (io/file appdata "jupyter/kernels"))))

(defn get-kernel-json [kernel-script-filename clj-args]
  (cheshire/generate-string {:display_name    "Lein-Clojure"
                             :language        "clojure"
                             :codemirror_mode "clojure"
                             :mimetype        "text/x-clojure"
                             :argv            ["python" (str kernel-script-filename) (first clj-args) "{connection_file}"]}))

(defn create-kernel [kernel-dir & clj-args]
  (let [kernel-json (io/file kernel-dir "lein-clojure" "kernel.json")
        kernel-script (io/file kernel-dir "lein-clojure" "leinclojure.py")]
    (io/make-parents kernel-json)
    (spit (str kernel-json) (get-kernel-json kernel-script clj-args))
    (spit (str kernel-script) python-kernel-script)))

(def architecture-not-yet-supported "You system is not supported by lein jupyter.
the current supported systems are Linux Mac and Windows (In that order).")

(defn install-kernel
  "Install the lein-clojure kernel.  If no argument passed, it
  will install the kernel in the userspace as specified at
  http://jupyter-client.readthedocs.io/en/latest/kernels.html.
  If an argument is passed, it will install the kernel in that
  directory.

  The kernel will be installed at <location>/lein-clojure."
  [& args]
  (let [os (get-os)]
    (if (nil? os)
      (println architecture-not-yet-supported)
      (apply create-kernel (if (starts-with? (first args) "-")
                             (cons (get-platform-specific-kernel-dir os (System/getenv)) args)
                             (cons (io/as-file (first args)) (rest args)))))))

(defn kernel-installed?
  "return true if it is sensible to believe that kernel has properly been installed"
  []
  (let [kernel-dir (get-platform-specific-kernel-dir (get-os) (System/getenv))]
    (.exists kernel-dir)))