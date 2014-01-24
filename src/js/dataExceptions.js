function ServiceException(err) {
    throw "#java.lang.IllegalStateException:" + err + "#";
}

function ValidationException(prop, err) {
    throw "#de.zeos.zen2.data.ValidationException:" + prop + "," + err + "#";
}