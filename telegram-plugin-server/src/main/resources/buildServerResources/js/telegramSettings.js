
Telegram = {};

Telegram.SettingsForm = OO.extend(BS.AbstractPasswordForm, {
    setupEventHandlers: function() {
        var that = this;
        $('testConnection').on('click', this.testConnection.bindAsEventListener(this));

        this.setUpdateStateHandlers({
            updateState: function() {
                that.storeInSession();
            },
            saveState: function() {
                that.submitSettings();
            }
        });
    },

    /** This method required for teamcity javascript events support (data changed and etc) */
    storeInSession: function() {
        $("submitSettings").value = 'storeInSession';
        BS.PasswordFormSaver.save(this, this.formElement().action, BS.StoreInSessionListener);
    },

    submitSettings: function() {
        $("submitSettings").value = 'store';
        this.removeUpdateStateHandlers();
        BS.PasswordFormSaver.save(this, this.formElement().action,
            OO.extend(BS.ErrorsAwareListener, this.createErrorListener()));
        return false;
    },

    createErrorListener: function() {
        var that = this;
        return {
            onEmptyBotTokenError: function(elem) {
                $("errorBotToken").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("botToken"));
            },
            onCompleteSave: function(form, responseXML, err) {
                BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
                if (!err) {
                    BS.XMLResponse.processRedirect(responseXML);
                } else {
                    that.setupEventHandlers();
                }
            }
        }
    },

    testConnection: function () {
        $("submitSettings").value = 'testConnection';
        var listener = OO.extend(BS.ErrorsAwareListener, this.createErrorListener());
        var oldOnCompleteSave = listener['onCompleteSave'];
        listener.onCompleteSave = function (form, responseXML, err) {
            oldOnCompleteSave(form, responseXML, err);
            if (!err) {
                form.enable();
                if (responseXML) {
                    var res = responseXML.getElementsByTagName("testConnectionResult")[0].
                        firstChild.nodeValue;
                    var success = res.lastIndexOf("bot name", 0) === 0;
                    BS.TestConnectionDialog.show(success, res, $('testConnection'));
                }
            }
        };
        BS.PasswordFormSaver.save(this, this.formElement().action, listener);
    }
});
