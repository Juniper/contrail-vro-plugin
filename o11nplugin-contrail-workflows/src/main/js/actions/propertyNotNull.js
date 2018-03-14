if(!item) return false;
return eval('item.' + parameterPath) != null;