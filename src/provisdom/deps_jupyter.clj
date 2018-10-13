(ns provisdom.deps-jupyter
  (:require [provisdom.deps-jupyter.extension :refer [install-and-enable-extension]]
            [provisdom.deps-jupyter.kernel :refer [install-kernel
                                                   kernel-installed?]]
            [clojupyter.core :as j])
  (:import [org.apache.commons.exec CommandLine
                                    DefaultExecutor
                                    PumpStreamHandler]))



(defn add-to-system-environment [new-envs]
  (let [env (apply hash-map (mapcat (fn [[x y]] [x y]) (System/getenv)))]
    (into env new-envs)))

(defn get-jupyter-command [jupyter sub-command jupyter-arguments]
  (let [all-arguments (into [sub-command] jupyter-arguments)
        add-args #(.addArgument %1 %2)]
    (reduce add-args (new CommandLine jupyter) all-arguments)))

(defn start-jupyter-notebook [jupyter environ jupyter-arguments]
  (let [executor (new DefaultExecutor)
        stream-handler (new PumpStreamHandler System/out System/err System/in)
        cmd (get-jupyter-command jupyter "notebook" jupyter-arguments)]
    (.setStreamHandler executor stream-handler)
    (.execute executor cmd environ)))

(defn start-jupyter-lab [jupyter environ jupyter-arguments]
  (let [executor (new DefaultExecutor)
        stream-handler (new PumpStreamHandler System/out System/err System/in)
        cmd (get-jupyter-command jupyter "lab" jupyter-arguments)]
    (.setStreamHandler executor stream-handler)
    (.execute executor cmd environ)))

(defn notebook [jupyter cwd & args]
  (if (not (kernel-installed?))
    (println "It seems you have not installed the lein-jupyter kernel.  "
                              "You should run `lein jupyter install-kernel`."))
  (let [new-env {"PROJECT_WORKING_DIRECTORY" cwd}
        env (add-to-system-environment new-env)]
    (start-jupyter-notebook jupyter env args)))

(defn lab [jupyter cwd & args]
  (if (not (kernel-installed?))
    (println "It seems you have not installed the lein-jupyter kernel.  "
             "You should run `lein jupyter install-kernel`."))
  (let [new-env {"PROJECT_WORKING_DIRECTORY" cwd}
        env (add-to-system-environment new-env)]
    (start-jupyter-lab jupyter env args)))

(defn jupyter
  "Leiningen's jupyter integration.

  To use this leiningen pluging, you need to have jupyter notebook
  installed.  See http://jupyter.org/ for instructions.

  Once you have jupyter notebook installed, you will need to run the
  `lein jupyter install-kernel` command once.  This will install the
  `lein-clojure` kernel to your jupyter installation.

  Afterward, `lein jupyter notebook` will launch a jupyter notebook
  in the same fashion then `jupyter notebook`.  The `lein-clojure`
  kernel in the notebook will then be hooked in the current project.

  Under the hood, lein-jupyter uses the excellent clojupyter clojure
  kernel.  Consequently, you can access any of its functionalities.

  Commands:
    notebook:
      Starts jupyter notebook and links jupyter notebook's kernel
      to the current project.  All extra parameters will be passed
      to `jupyter notebook`.  For instance `jupyter notebook --port=9876`
      will start jupyter notebook on port 9876
    install-kernel:
      Install jupyter notebook's clojure kernel.  This needs to be run
      once.  If no argument passed, the kernel will be installed in the
      jupyter user space.  If an argument is passed, the kernel will
      be installed into the specified directory.
    uninstall-kernel:
      Uninstall jupyter notebook's clojure kernel.
  "
  ([sub-command & args]
   (let [cwd (-> (java.io.File. ".") .getAbsolutePath)
         jupyter (first args)]
     (case sub-command
       "install-kernel" (do
                          (apply install-kernel (next args))
                          (install-and-enable-extension jupyter))
       "uninstall-kernel" (println (str "Not yet implemented.  You can use "
                                        "'jupyter kernelspec uninstall lein-clojure' "
                                        "to uninstall the kernel manually."))
       "notebook" (apply notebook jupyter cwd (next args))         ;; main entry
       "lab" (apply lab jupyter cwd (next args))
       "kernel" (apply j/-main args)             ;; hidden kernel
       (println (:doc (meta #'jupyter))))))
  ([]
   (println (:doc (meta #'jupyter)))))

(def -main jupyter)