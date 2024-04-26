(ns babashka.bbin.gen-script
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [fipp.edn :as fipp]))

(def bbin-deps (some-> (slurp "deps.edn") edn/read-string :deps))

(def version
  (-> (slurp "deps.edn") edn/read-string
      :aliases :neil :project :version))

(def prelude-template
  (str/triml "
#!/usr/bin/env bb

; :bbin/start
;
; {:coords {:bbin/url \"https://raw.githubusercontent.com/babashka/bbin/%s/bbin\"}}
;
; :bbin/end

(babashka.deps/add-deps
  '{:deps %s})
"))

(def min-bb-version
  (some-> (slurp "bb.edn") edn/read-string :min-bb-version))

(def meta-template
  `[(~'ns ~'babashka.bbin.meta)
    (~'def ~'min-bb-version
      "This def was generated by the bbin build script."
      ~min-bb-version)
    (~'def ~'version
      "This def was generated by the bbin build script."
      ~version)])

(def meta-str
  (str/join "\n" (map pr-str meta-template)))

(def prelude-str
  (let [lines (-> (with-out-str (fipp/pprint bbin-deps {:width 80})) str/split-lines)]
    (format prelude-template
            (if (str/ends-with? version "-SNAPSHOT") "main" (str "v" version))
            (str/join "\n" (cons (first lines) (map #(str "          " %) (rest lines)))))))

(def all-scripts
  [prelude-str
   meta-str
   (slurp "src/babashka/bbin/dirs.clj")
   (slurp "src/babashka/bbin/protocols.clj")
   (slurp "src/babashka/bbin/specs.clj")
   (slurp "src/babashka/bbin/util.clj")
   (slurp "src/babashka/bbin/git.clj")
   (slurp "src/babashka/bbin/deps.clj")
   (slurp "src/babashka/bbin/scripts/common.clj")
   (slurp "src/babashka/bbin/scripts/git_dir.clj")
   (slurp "src/babashka/bbin/scripts/http_file.clj")
   (slurp "src/babashka/bbin/scripts/http_jar.clj")
   (slurp "src/babashka/bbin/scripts/local_dir.clj")
   (slurp "src/babashka/bbin/scripts/local_file.clj")
   (slurp "src/babashka/bbin/scripts/local_jar.clj")
   (slurp "src/babashka/bbin/scripts/maven_jar.clj")
   (slurp "src/babashka/bbin/scripts.clj")
   (slurp "src/babashka/bbin/migrate.clj")
   (slurp "src/babashka/bbin/cli.clj")])

(defn gen-script []
  (spit "bbin" (str/join "\n" all-scripts)))
