(defproject selfsame/hyper "0.3.32-SNAPSHOT"
  :description "om.next utils for terse markup."
  :url "http://github.com/selfsame/hyper"
  :license {:name "The MIT License (MIT)"
            :url "https://github.com/selfsame/pdfn/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [figwheel-sidecar "0.5.0-2" :scope "test"]]
  :main hyper.terse
  :source-paths ["src" "test"]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-2"]]
  :jar-exclusions [#"test" #"resources"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src" "test"]
              :figwheel {}
              :compiler {:main hyper.terse
                         :asset-path "js/compiled/out"
                         :output-to "resources/public/js/compiled/main.js"
                         :output-dir "resources/public/js/compiled/out"
                         :source-map-timestamp true }}]}
  :figwheel {:load-warninged-code true})
