(defproject mtnygard/lein-cucumber "1.0.8-SNAPSHOT"
  :description "Run cucumber-jvm specifications with leiningen"
  :url https://github.com/mtnygard/lein-cucumber
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [info.cukes/cucumber-clojure "1.2.5"]]
  :profiles {:cucumber     {:dependencies [[commons-io "2.4"]]
                            :plugins      [[mtnygard/lein-cucumber "1.0.8-SNAPSHOT"]]}
             :cuke-htmlrep [:cucumber {:cucumber
                                       {:formatter {:type :html
                                                    :path "target/report"}}}]}
  :eval-in :leiningen
  :license {:name         "Unlicense"
            :url          "http://unlicense.org/"
            :distribution :repo})
