(ns mell.compose
  (:require [conch.sh :refer [with-programs let-programs]]
            [environ.core :refer [env]]
            [clojure.string :as string])
  (:import [javax.mail.internet MimeMultipart 
            MimeMessage MimeBodyPart InternetHeaders
            InternetAddress]
           [javax.mail Transport]
           [java.io ByteArrayOutputStream]
           [org.jsoup Jsoup]))

(def editor (env :editor "nano"))

(defn mimeheaders [headermap]
  (let [hobj (InternetHeaders.)]
    (doseq [[k v] headermap] (.addHeader hobj k v))
    hobj))

(defn bodypart [headers ^bytes content]
  (MimeBodyPart. (mimeheaders headers) content))

(defn str->bodypart [headers content]
  (bodypart headers (.getBytes content)))

(defn markdown [istr]
  (with-programs [kramdown]
    (kramdown {:in istr})))

(defn highlight [istr lex]
  (with-programs [pygmentize]
    (pygmentize "-l" lex "-f" "html" "-O" "cssclass=src" {:in istr})))

(defn check-lang [code]
  (let [clines (string/split-lines code)]
    (when (= \` (ffirst clines))
      [(string/join "\n" (rest clines))
       (apply str (rest (first clines)))])))

(defn hl-filter "highlight. returns a jsoup!" [htmlstr]
  (let [soup (Jsoup/parse htmlstr)]
    (doseq [p (.select soup "pre")]
      (let [code (.text p)
            checked (check-lang code)]
        (when checked
          (.after p (apply highlight checked))
          (.remove p))))
    soup))

(def codestyles 
  {
   ".hll" "background-color: #ffffcc" 
   ".c" "color: #999988; font-style: italic" 
   ".err" "color: #a61717; background-color: #e3d2d2"  
   ".k" "color: #000000; font-weight: bold" 
   ".o" "color: #000000; font-weight: bold" 
   ".cm" "color: #999988; font-style: italic" 
   ".cp" "color: #999999; font-weight: bold; font-style: italic" 
   ".c1" "color: #999988; font-style: italic" 
   ".cs" "color: #999999; font-weight: bold; font-style: italic" 
   ".gd" "color: #000000; background-color: #ffdddd" 
   ".ge" "color: #000000; font-style: italic" 
   ".gr" "color: #aa0000" 
   ".gh" "color: #999999" 
   ".gi" "color: #000000; background-color: #ddffdd" 
   ".go" "color: #888888" 
   ".gp" "color: #555555" 
   ".gs" "font-weight: bold" 
   ".gu" "color: #aaaaaa" 
   ".gt" "color: #aa0000" 
   ".kc" "color: #000000; font-weight: bold" 
   ".kd" "color: #000000; font-weight: bold" 
   ".kn" "color: #000000; font-weight: bold" 
   ".kp" "color: #000000; font-weight: bold" 
   ".kr" "color: #000000; font-weight: bold" 
   ".kt" "color: #445588; font-weight: bold" 
   ".m" "color: #009999" 
   ".s" "color: #d01040" 
   ".na" "color: #008080" 
   ".nb" "color: #0086B3" 
   ".nc" "color: #445588; font-weight: bold" 
   ".no" "color: #008080" 
   ".nd" "color: #3c5d5d; font-weight: bold" 
   ".ni" "color: #800080" 
   ".ne" "color: #990000; font-weight: bold" 
   ".nf" "color: #990000; font-weight: bold" 
   ".nl" "color: #990000; font-weight: bold" 
   ".nn" "color: #555555" 
   ".nt" "color: #000080" 
   ".nv" "color: #008080" 
   ".ow" "color: #000000; font-weight: bold" 
   ".w" "color: #bbbbbb" 
   ".mf" "color: #009999" 
   ".mh" "color: #009999" 
   ".mi" "color: #009999" 
   ".mo" "color: #009999" 
   ".sb" "color: #d01040" 
   ".sc" "color: #d01040" 
   ".sd" "color: #d01040" 
   ".s2" "color: #d01040" 
   ".se" "color: #d01040" 
   ".sh" "color: #d01040" 
   ".si" "color: #d01040" 
   ".sx" "color: #d01040" 
   ".sr" "color: #009926" 
   ".s1" "color: #d01040" 
   ".ss" "color: #990073" 
   ".bp" "color: #999999" 
   ".vc" "color: #008080" 
   ".vg" "color: #008080" 
   ".vi" "color: #008080" 
   ".il" "color: #009999" 
   })

(defn style-filter
  [soup]
  (doseq [[cls style] codestyles]
    (doseq [el (.select soup cls)]
      (.attr el "style" style)))
  soup)

(defn markup [body]
  (-> body markdown hl-filter style-filter str))

(defn compose [session addrs]
  (with-programs [sh]
    (spit "message-tmp.md" (str "To: " (string/join ", " addrs)
                                "\r\nSubject: \r\n\r\n")) 
    (sh "-c" (str editor " message-tmp.md")) 
    (let [message (slurp "message-tmp.md")
          [headers-raw body] (string/split message #"\r?\n\r?\n" 2)
          headers (into {} (map #(mapv string/trim (string/split % #":" 2)) (string/split-lines headers-raw)))
          textpart (str->bodypart {"Content-Type" "text/plain"} body)
          htmlpart (str->bodypart {"Content-Type" "text/html"} (markup body)) 
          multipart (doto (MimeMultipart. "alternative")
                      (.addBodyPart textpart)
                      (.addBodyPart htmlpart))
          mimemessage (MimeMessage. session)]
      (.setContent mimemessage multipart) 
      (doseq [[k v] headers] (.addHeader mimemessage k v))
      mimemessage)))
