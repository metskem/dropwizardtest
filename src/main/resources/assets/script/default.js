  function showData(yourUrl){
  var Httpreq = new XMLHttpRequest(); // a new request
  Httpreq.open("GET",yourUrl,false);
  Httpreq.send(null);
  var responseText = Httpreq.responseText;
  var json = JSON.parse(responseText);
  var arrayLength = json.length;
  var table = document.getElementById("myTable");
  for (var i = 0; i < arrayLength; i++) {
     console.log("this is id and content: " + json[i].id + " " + json[i].content);
     var row = table.insertRow(i+1);
     var cell1 = row.insertCell(0);
     var cell2 = row.insertCell(1);
     cell1.innerHTML = json[i].id;
     cell2.innerHTML = json[i].content;
  }
