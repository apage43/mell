# mell
## stupid markdown/javamail tricks

Create a ~/.mellrc 

This is almost exactly the properties object passed to javamail's
Session.getInstance, except can include a password and is a clojure map.

See `mellrc.sample` for an example.

    # type a mail in markdown using $EDITOR and send it
    # the markdown will be sent as the text/plain part
    $ lein run compose recipient@place.com
    # type a mail in markdown using $EDITOR and save it to [Gmail]/Drafts IMAP folder
    $ lein run draft recipient@place.com

### dependencies that aren't magic

 * pygments
 * kramdown
