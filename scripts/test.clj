(require '[babashka.fs :as fs]
         '[clojure.string :as str]
         '[clojure.test :as t])

(def namespaces
  (vec (for [f (fs/glob "test" "**/*_test.clj")]
         (-> (str (fs/relativize "test" f))
             (str/replace #"\.clj$" "")
             (str/replace "/" ".")
             (str/replace "_" "-")
             symbol))))

(if (empty? namespaces)
  (println "No tests yet.")
  (do (apply require namespaces)
      (let [{:keys [fail error]} (apply t/run-tests namespaces)]
        (System/exit (if (zero? (+ fail error)) 0 1)))))
