(defproject snp-assess "0.0.1-SNAPSHOT"
  :description "Deep sequence variation assessment with Hadoop."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/algo.generic "0.1.0"]
                 [cascalog "1.8.5"]
                 [incanter/incanter-core "1.3.0-SNAPSHOT"]
                 [incanter/incanter-charts "1.3.0-SNAPSHOT"]
                 [com.leadtune/clj-ml "0.2.2"]
                 [fs "1.1.2"]
                 [clj-yaml "0.3.1"]
                 [ordered "1.0.0"]
                 [doric "0.7.0-SNAPSHOT"]
                 [bcbio.variation "0.0.1-SNAPSHOT"]
                 [org.biojava/biojava3-core "3.0.2"]
                 [org.clojars.chapmanb/gatk "1.5.2"]
                 [org.clojars.chapmanb/picard "1.64"]]
  :dev-dependencies [[org.apache.hadoop/hadoop-core "0.20.2-dev"]
                     [midje "1.3.0" :exclusions [org.clojure/clojure]]]
  :repositories {"biojava" "http://www.biojava.org/download/maven/"}
  :run-aliases {:snp-data snp-assess.core
                :off-target snp-assess.off-target
                :min-coverage snp-assess.min-coverage
                :classify snp-assess.classify
                :classify-eval snp-assess.classify-eval
                :classify-read snp-assess.classify-read
                :call snp-assess.call
                :reference snp-assess.reference}
  :jvm-opts ["-Xmx2g"]
  :aot [snp-assess.core])
