(ns snp-assess.classify-test
  (:use [midje.sweet]
        [snp-assess.classify]
        [snp-assess.classify-eval :only [summarize-assessment]]
        [snp-assess.config])
  (:require [fs.core :as fs]))

(let [data-dir (str (fs/file "test" "data"))
      vrn-file (str (fs/file data-dir "coverage_pos" "pos.tsv"))
      data-file (str (fs/file data-dir "raw" "raw_variations.tsv"))
      config (-> default-config
                 (assoc-in [:classification :naive-min-score] 1.2)
                 (assoc-in [:classification :max-neg-pct] 5.0)
                 (assoc-in [:classification :pass-thresh] 0.0))]
  (facts "Read variant positions from file"
    (read-vrn-pos vrn-file 5.0) => {["HXB2_IUPAC_93-5" 951 "A"] 1.0,
                                    ["HXB2_IUPAC_93-5" 953 "T"] 5.0}
    (read-vrn-pos vrn-file 3.0) => {["HXB2_IUPAC_93-5" 951 "A"] 1.0})

  (facts "Classification data from file"
    (let [data (prep-classifier-data data-file vrn-file config)]
      (count data) => 2 ; Total minority variants
      (count (first data)) => 4 ; items at each position
      (map last data) => [1 0] ; classifications
      (first data) => (contains [0.264 1])))

  (facts "Build a full classifier from file data"
    (let [c (train-classifier data-file vrn-file config)]
      (map float (.coefficients c)) => [0.0 0.0 0.0 0.0 0.5]
      (.toString c) => (contains "Linear Regression Model")
      ((classifier-checker c config) 20 1.0E-3 200) => true))

  (facts "Assess a classifier based on variant calling ability"
    (let [c (train-classifier data-file vrn-file config)
          get-class (fn [p r e] (:class (assign-position-type p r 0 e config)))
          assess-data (assess-classifier data-file vrn-file c config)]
      (get-class ["chr" 10] {"G" 99.0} {}) => :true-positive
      (get-class ["chr" 10] {"G" 95.0 "A" 5.0} {}) => :false-positive
      (get-class ["chr" 10] {"G" 95.0 "A" 5.0} {["chr" 10 "A"] 5.0}) => :true-positive
      (get-class ["chr" 10] {"G" 83.0 "A" 17.0} {["chr" 10 "A"] 5.0}) => :false-negative
      (get-class ["chr" 10] {"G" 90.0 "A" 5.0 "C" 3.0} {["chr" 10 "A"] 5.0}) => :false-negative
      (get-class ["chr" 10] {"G" 99.0} {["chr" 10 "A"] 5.0}) => :false-negative
      (map :class assess-data) => [:false-negative :false-positive :false-negative]
      (first (summarize-assessment assess-data)) => [1.0 {:false-negative 1} []])))

(facts "Remap raw data for classification"
  (finalize-raw-data {:qual 20 :kmer-pct 1.0E-4 :map-score 100} :test default-config) =>
    (contains [(roughly 0.516) (roughly 9.0E-4) 0.4 :test])
    (finalize-raw-data {:qual 30 :kmer-pct 1.0E-2 :map-score 50} :test2 default-config) =>
    (contains [(roughly 0.8387) (roughly 0.0999) 0.2 :test2]))

(let [data-dir (str (fs/file "test" "data"))
      data-file (str (fs/file data-dir "raw" "raw_variations_count.tsv"))
      config (-> default-config
                 (assoc-in [:classification :assess-bases] nil)
                 (assoc :min-freq 0.0))]
  (facts "Read raw data collapsed by counts per unique read."
    (let [raw-freqs (raw-data-frequencies data-file config)]
      (count raw-freqs) => 3
      (ffirst raw-freqs) => ["HXB2" 5] ; read position
      (-> raw-freqs first last) => 71174 ; number of total reads
      (-> raw-freqs first second (get "G")) => (roughly 99.9747)
      (-> raw-freqs first second (get "T")) => (roughly 0.009835))))
