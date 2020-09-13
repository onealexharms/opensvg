(ns open-circuitry.svg-test
 (:require
  [clojure.java.io :as io]
  [clojure.test :refer [deftest is]]
  [dali.io]
  [net.cgrand.enlive-html :as enlive]
  [open-circuitry.svg :refer [dali-rendering]]))

(deftest rendering-fails-if-no-width-is-given
  (is (thrown-with-msg? Exception
                        #"A board needs a :width attribute"
                        (dali-rendering [:open-circuitry/board
                                         {:height 7}]))))

(deftest rendering-fails-if-no-height-is-given
  (is (thrown-with-msg? Exception
                        #"A board needs a :height attribute"
                        (dali-rendering [:open-circuitry/board
                                         {:width 9}]))))

(defn svg-element [board selector]
  (-> board
      dali-rendering
      dali.io/render-svg-string
      (.getBytes)
      io/input-stream
      enlive/xml-parser
      (enlive/select selector)
      first))

(defn svg-attributes [board selector]
  (:attrs (svg-element board selector)))

(deftest has-a-cutout-toolpath
  (let [board [:open-circuitry/board {:width 10 :height 10}]
        cutout-toolpath (svg-element board [:g#cutout-toolpath])]
   (is cutout-toolpath)))

(deftest cutout-has-zero-origin 
  (let [board [:open-circuitry/board {:width 10 :height 20}]
        {:keys [x y]} (svg-attributes board [:g#cutout-toolpath :rect])]
    (is (= "0" x))
    (is (= "0" y))))

(deftest cutout-size-matches-board-size
  (let [board [:open-circuitry/board {:width 10 :height 20}]
        {:keys [width height]} (svg-attributes board [:g#cutout-toolpath :rect])]
    (is (= "10" width))
    (is (= "20" height))))

(deftest cutout-has-white-fill
  (let [board [:open-circuitry/board {:width 10 :height 20}]
        {:keys [fill]} (svg-attributes board [:g#cutout-toolpath :rect])]
    (is (= "white" fill))))

(deftest cutout-has-red-stroke
  (let [board [:open-circuitry/board {:width 10 :height 20}]
        {:keys [stroke]} (svg-attributes board [:g#cutout-toolpath :rect])]
    (is (= "red" stroke))))

(deftest a-50x100-board-rendering-is-50mm-wide-100mm-high
  (let [board [:open-circuitry/board {:width 50 :height 100}]
        {:keys [width height]} (svg-attributes board [:svg])]
    (is (= "50mm" width))
    (is (= "100mm" height))))

(deftest viewbox-ensures-rendered-units-are-millimeters
  (let [board [:open-circuitry/board {:width 57 :height 142}]
        {:keys [viewBox]} (svg-attributes board [:svg])]
    (is (= "0 0 57 142" viewBox))))
