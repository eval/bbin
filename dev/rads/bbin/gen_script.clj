(ns rads.bbin.gen-script
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [fipp.edn :as fipp]))

(def bbin-deps (some-> (slurp "deps.edn") edn/read-string :deps))

(def prelude-template
  (str/triml "
#!/usr/bin/env bb

; :bbin/start
;
; {:coords {:bbin/url \"https://raw.githubusercontent.com/rads/bbin/main/bbin\"}}
;
; :bbin/end

(babashka.deps/add-deps
  '{:deps %s})
"))

(def prelude-str
  (let [lines (-> (with-out-str (fipp/pprint bbin-deps {:width 80})) str/split-lines)]
    (format prelude-template
            (str/join "\n" (cons (first lines) (map #(str "          " %) (rest lines)))))))

(defn gen-script []
  (let [trust (slurp "src/rads/bbin/trust.clj")
        curl (slurp "src/rads/bbin/curl.clj")
        git (slurp "src/rads/bbin/git.clj")
        infer (slurp "src/rads/bbin/infer.clj")
        bbin (slurp "src/rads/bbin.clj")]
    (spit "bbin" (str/join "\n" [prelude-str
                                 trust curl git infer
                                 bbin]))))
