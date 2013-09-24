(def VERSION (.trim (slurp "VERSION")))
(def MODULES (for [m (.split (slurp "MODULES") "\newline")](.trim m)))
(def DEPENDENCIES (for [m MODULES] [(symbol (str "courtauction/" m)) VERSION]))

(eval `(defproject courtauction ~VERSION
  :dependencies [~@DEPENDENCIES]
  :plugins [[~'lein-sub "0.2.1"]]  
  :min-lein-version "2.0.0"
  :sub [~@MODULES]
  ))