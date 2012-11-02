(ns mell.mail
  (:require [mell.util :as mu])
  (:import [javax.mail Session Store Folder Message Transport]
           [javax.mail.internet MimeMultipart]))

(defn authenticator [conf]
  (proxy [javax.mail.Authenticator] []
    (getPasswordAuthentication []
      (javax.mail.PasswordAuthentication.
        (:user (:mail conf))
        (:password (:mail conf))))))

(defn session [conf]
  (Session/getInstance (mu/to-props conf) (authenticator conf)))

(defn connect [sess]
  (doto (.getStore sess)
    (.connect nil (get (.getProperties sess) "mail.password"))))

(defn folder
  ([conn fl]
     (folder conn fl Folder/READ_ONLY))
  ([conn fl access]
     (doto (.getFolder conn fl) (.open access))))

(defn only-mime [parts mimetype]
  (filter #(.isMimeType % mimetype) parts))

(defn parts [multipart]
  (map #(.getBodyPart multipart %) (range (.getCount multipart))))

(defn text-part [content]
  (if (instance? MimeMultipart content)
    (let [plainpart (first (only-mime (parts content) "text/plain"))]
      (if plainpart (.getContent plainpart) ""))
    content))

(defn send-msg [message]
  (Transport/send message))

(defn insert [folder message]
  (.addMessages folder (into-array Message [message])))
