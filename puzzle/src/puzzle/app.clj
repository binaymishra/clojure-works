(ns puzzle.app
  (:gen-class)
  (:require
    [ring.adapter.jetty :as jetty]
    clojure.pprint clojure.java.io))

;;Reads CSV File and stores data into a PersistantVector.
(def csv-file
  (vec
    (rest
      (with-open
        [rdr (clojure.java.io/reader "resources/csv_data.csv")]
        (reduce conj [] (line-seq rdr))))))
;;output=>["72144305,11050,1000,2020,control" "72144777,20000,0,3000,test" "72145239,5000,500,1500,unmanaged"]

;;Reads PSV File and stores data into a PersistantVector.
(def psv-file
  (vec
    (rest
      (with-open
        [rdr (clojure.java.io/reader "resources/psv_data.psv")]
        (reduce conj [] (line-seq rdr))))))
;;output=>["72144305|110.0|20.5|10.2|CONTROL" "72144777|200.0|30.0|0.0|TEST" "72145239|50.0|14.8|2.0|UNMANAGED"]

(def csv-data (vec (map #(into [] (vec (re-seq #"[\w]+" %1))) csv-file)))
;;output=>[["72144305" "11050" "1000" "2020" "control"] ["72144777" "20000" "0" "3000" "test"] ["72145239" "5000" "500" "1500" "unmanaged"]]

(def psv-data (vec (map #(into [] (vec (re-seq #"[\w\d.]+" %1))) psv-file)))
;;output=>[["72144305" "110.0" "20.5" "10.2" "CONTROL"] ["72144777" "200.0" "30.0" "0.0" "TEST"] ["72145239" "50.0" "14.8" "2.0" "UNMANAGED"]]

;;Constructing runa-record from csv-data
(defn make-runa-record 
  [input]
  {"order-id" (input 0)
   "unit-price-dollars" (/ (java.lang.Double/parseDouble (input 1) ) 100.0) 
   "merchant-discount-dollars" (/ (java.lang.Double/parseDouble (input 2)) 100.0)
   "runa-discount-dollars" (/ (java.lang.Double/parseDouble(input 3)) 100.0)
   "session-type" (input 4) })

;;Constructing marchant-record from psv-data
(defn make-merchant-record 
  [input]
  {"order-id" (input 0)
   "unit-price-dollars" (/ (java.lang.Double/parseDouble (input 1)) 1.0)
   "runa-discount-dollars" (/ (java.lang.Double/parseDouble (input 2)) 1.0)
   "merchant-discount-dollars" (/ (java.lang.Double/parseDouble (input 3)) 1.0)
   "session-type" (input 4)})

(def runa-data (map #(make-runa-record %1) csv-data))
(def merchant-data (map #(make-merchant-record %1) psv-data))
(def all-data (map #(assoc {} "runa-data" %1 "merchant-data" %2) runa-data merchant-data))

;;Sort data:input parameters (`session-type-desc`, `order-id-asc`, `unit-price-dollars-asc`)
(defn sort-data
  [all-data sort-order]
  (sort-by :sort-order all-data))

;;Runa Summary and Merchant Summary.
(defn summaries
   [data]
   {"runa-summary" 
          {"unit-price-dollars" (apply + (map #((% "runa-data") "unit-price-dollars" ) data))
            "merchant-discount-dollars" (apply + (map #((% "runa-data") "merchant-discount-dollars") data))
             "runa-discount-dollars" (apply + (map #((% "runa-data") "runa-discount-dollars") data))
          }
	  "merchant-summary" 
					{ "unit-price-dollars" (apply + (map #((% "merchant-data") "unit-price-dollars") data))
             "merchant-discount-dollars" (apply + (map #((% "merchant-data") "merchant-discount-dollars") data))
              "runa-discount-dollars" (apply + (map #((% "merchant-data") "runa-discount-dollars") data))
          }
		})

(defn final-summary 
  [data]
  (clojure.pprint/pprint data))

(defn -main
  [& args]
 ;; (def server (jetty/run-jetty #'app {:port 8080 :join? false}))
  (final-summary all-data)
 )