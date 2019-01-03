var exec = require('cordova/exec');


module.exports={
    receivedCall : function () {
        exec(null, null, 'RoversLoungeHelper', 'receivedCall', null);
    },
    wakeUp:function(){
        exec(null, null, 'RoversLoungeHelper', 'wakeUp', null)
    },
    sleepAgain:function(){
        exec(null,null, 'RoversLoungeHelper', 'sleepAgain', null)
    },
    restoreAudio:function(){
        exec(null,null,'RoversLoungeHelper','normalSound',null);
    },
    loudSpeaker:function(){
        exec(null,null,'RoversLoungeHelper','loudSpeaker',null);
    },
    earPiece:function(){
        exec(null,null,'RoversLoungeHelper','earPiece',null);
    }
};