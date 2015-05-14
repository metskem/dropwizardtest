function showData(yourUrl){
  var Httpreq = new XMLHttpRequest(); // a new request
  Httpreq.open("GET",yourUrl,false);
  Httpreq.send(null);
  var responseText = Httpreq.responseText;
  var json_obj = JSON.parse(Get(yourUrl));
  console.log("this is id and content: " + json_obj.id + " " + json_obj.content);
}