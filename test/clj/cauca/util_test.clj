(ns cauca.util-test
  (:use [clojure test]
        [cauca.util]
        ) 
  )

(deftest validation-test 
; nil false
  (is (= false (validation nil 1 5 #"[a-z]+" true)))
; string true
  (is (= true (validation "abc" 1 5 #"[a-z]+" true)))
; string min false
  (is (= false (validation "abc" 4 5 #"[a-z]+" true)))
; string max false
  (is (= false (validation "abc" 1 2 #"[a-z]+" true)))
; string regex false
  (is (= false (validation "abc" 1 2 #"[a-b]+" true)))
  (is (= false (validation "abc" 1 2 #"[a-z]+" false)))
; number true
  (is (= true (validation 4 1 6 nil nil)))
; number min false  
  (is (= false (validation 4 5 6 nil nil)))
; number max false  
  (is (= false (validation 4 1 3 nil nil)))
  )