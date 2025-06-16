const xhr = new XMLHttpRequest();
const nm = document.getElementsByTagName("header")[0].textContent;
const configuration = {
  iceServers: [
    { urls: "stun:stun.l.google.com:19302" },
    {
      urls: 'turn:openrelay.metered.ca:80',
      username: 'openrelayproject',
      credential: 'openrelayproject'
    }
  ]
};
xhr.open("POST", "http://localhost:8080", true);
xhr.setRequestHeader("Content-type", "text/plain");
xhr.send("Name: " + nm);
console.log("Starting " + nm);

const peerConn = new RTCPeerConnection(configuration);
xhr.onreadystatechange = function() {
    console.log("reaching here in the code");
    if (xhr.readyState === 4 && xhr.status === 200) {
      console.log("Processing");
      console.log(xhr.responseText);
      processResponse(xhr.responseText);
      console.log("Checking for the ice candidate");
              // ICE gathering is complete, create and send the offer
            peerConn.createOffer()
            .then(offer => {
              // Set the local description of the offer
              return peerConn.setLocalDescription(offer);
            })
            .then(() => {
              peerConn.onicecandidate = event => {
                console.log("Gotten an ice candidate event");
                  if (event.candidate) {
                  // Send the ICE candidate to the remote peer via signaling server
                  console.log(event.candidate);
                  sendIceToServer(event.candidate);
                  }
                };
              // Register the onicegatheringstatechange event listener
              peerConn.onicegatheringstatechange = event => {
                const iceGatheringState = peerConn.iceGatheringState;
                console.log('ICE gathering state:', iceGatheringState);
        
                if (iceGatheringState === 'complete') {
                  console.log('ICE gathering complete');
        
                  // Send the offer to the server
                  sendOfferToServer(peerConn.localDescription);
              };
            }
            })
            .catch(error => {
              console.error('Error creating offer:', error);
            });
        }
      };

    function sendIceToServer(description){
    xhr.open("POST", "http://localhost:8080", true);
    xhr.setRequestHeader("Content-type", "text/plain");
    xhr.send("ICE: "+nm+": " + JSON.stringify(description));
  
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4 && xhr.status === 200) {
        console.log("Processing");
        console.log(xhr.responseText);
      }
    };
  }
 // Send the RTC details to the server
 function sendOfferToServer(description) {
  xhr.open("POST", "http://localhost:8080", true);
  xhr.setRequestHeader("Content-type", "text/plain");
  xhr.send("Offer: " +nm+": " + JSON.stringify(description));

  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4 && xhr.status === 200) {
      console.log("Processing");
      console.log(xhr.responseText);
      longpollingfunc(xhr);
    }
  };
}

function longpollingfunc(xhrr) {
  // Set up the long polling request
  // We want to send a null request to the server
  // Then we wait for a response
  // After the response, we process the response and act on it
  // Then we send a request again to start the process all over again

  // The plan is to receive different responses from the server
  // Act based on these responses
  // Make changes to the HTML page

  var s = document.getElementById("message").value;
  console.log("making another request");
  if(s != ''){
      var nm = document.getElementsByTagName("header")[0].textContent;
      xhrr.open("POST", "http://localhost:8080", true);
      xhrr.setRequestHeader("Content-type", "text/plain");
      xhrr.send("Message: "+nm+": "+s);
      document.getElementById("message").value = "";
  }else{
      var nm = document.getElementsByTagName("header")[0].textContent;
      console.log("making a null request");
      xhrr.open("POST", "http://localhost:8080", true);
      xhrr.setRequestHeader("Content-type", "text/plain");
      xhrr.send("null: "+nm);
  }
  xhrr.onreadystatechange = function() {
      if (xhrr.readyState === 4 && xhrr.status === 200) {
          console.log(xhrr.responseText);
          processResponse(xhrr.responseText);
      }
  };
}

function processResponse(response) {
  console.log("The response is: "+response)
  var nm = document.getElementsByTagName("header")[0].textContent;
  var sidePanel = document.getElementById("targetDiv1");
  var res = response.split(" ");
  switch (res[0]) {
    case "List:":
      for (var i = 2; i < res.length; i++) {
        res[1] = res[1].concat(" ").concat(res[i]);
      }
      console.log(res[1]);
      var resl = res[1].split("&");
      var rest = resl[0].split(",");
      for(var i=0; i<rest.length; i++){
        var newDiv = document.createElement("div");
        newDiv.className = "t-item";
        newDiv.textContent = rest[i];
        sidePanel.appendChild(newDiv);
        console.log(newDiv);
      }
      if(resl[1] != null){
        var resu = resl[1].split(" ");
        var result;
        if(resu[0] != "MList:"){
          for (var i = 1; i < resu.length; i++) {
            result = resu[0].concat(" ").concat(resu[i]);
          }
        }else{
          for (var i = 2; i < resu.length; i++) {
            result = resu[1].concat(" ").concat(resu[i]);
          }
        }
        
        if(result != null){
          var resul = result.split(",");
          for(var i=0; i<resul.length; i++){
          var trg = document.getElementById("chat");
          var newDiv = document.createElement("p");
          newDiv.textContent = resul[i];
          trg.appendChild(newDiv);
          }
        }
      }
      
      longpollingfunc(xhr);
      break;
    case "Update:":
      for (var i = 2; i < res.length; i++) {
        res[1] = res[1].concat(" ").concat(res[i]);
      }
      console.log(res[1]);
      console.log(nm);
      if(res[1].trim() != nm.trim()){  
        var newDiv = document.createElement("div");
        newDiv.className = "t-item";
        newDiv.textContent = res[1];
        sidePanel.appendChild(newDiv);
      }
      
      longpollingfunc(xhr);
      break;
    case "Message:":
        var trg = document.getElementById("chat");
        for (var i = 2; i < res.length; i++) {
          res[1] = res[1].concat(" ").concat(res[i]);
        }
        var newDiv = document.createElement("p");
        newDiv.textContent = res[1];
        trg.appendChild(newDiv);
        
        longpollingfunc(xhr);
        break;
    case "Offer:":
          for (var i = 2; i < res.length; i++) {
            res[1] = res[1].concat(" ").concat(res[i]);
          }
        //create an answer with the peer connection and send it to them
          handleSdpOffer(res[1]);
        break;
    case "Answer:":
        for (var i = 2; i < res.length; i++) {
          res[1] = res[1].concat(" ").concat(res[i]);
         }
        handleSdpAnswer(res[1]);
        break;
    case "ICE:":
          handleIceCandidate(res[1]);
       break;
  }
}

// Function to handle the SDP offer from the streaming client
function handleSdpOffer(offer) {
  // // Create a peer connection object
  // const peerConnection = new RTCPeerConnection();
  divide = offer.split(" ");
  ofDetails = "";
  for ( i = 2; i < divide.length; i++) {
      ofDetails += divide[1].append(" ").append(divide[i]);
  }
  console.log("The offer details is: "+ofDetails);



  // Set the remote description
  peerConn.setRemoteDescription(new RTCSessionDescription(JSON.parse(ofDetails)))
    .then(() => {
      // Create an SDP answer
      return peerConn.createAnswer();
    })
    .then(answer => {
      // Set the local description and send the answer to the streaming client
      return peerConn.setLocalDescription(answer);
    })
    .then(() => {
      // Send the SDP answer to the streaming client via signaling server
      const sdpAnswer = JSON.stringify(peerConn.localDescription);
      xhr.open('POST', 'http://localhost:8080', true);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.send("Answer: "+divide[0]+": "+sdpAnswer);
    })
    .catch(error => {
      console.error('Error creating or sending answer:', error);
    });
}

// Function to handle ICE candidates from the streaming client
function handleIceCandidate(candidate) {
  // Add the remote ICE candidate to the peer connection
  // const peerConnection = new RTCPeerConnection();
  peerConn.addIceCandidate(new RTCIceCandidate(candidate))
  .then(() => {
        // Add an event listener for the connection state change
    peerConn.onconnectionstatechange = event => {
      const connectionState = peerConn.connectionState;
      if (connectionState === 'connected') {
        // Connection is successfully established
        console.log('Connection established on line 212!');
      }
    };
  })
    .catch(error => {
      console.error('Error adding ICE candidate:', error);
    });
}

// Function to handle the SDP answer from the streaming client
function handleSdpAnswer(answer) {
  console.log("The answer is: "+answer);
  // Set the remote description
  peerConn.setRemoteDescription(new RTCSessionDescription(JSON.parse(answer)))
  .then(() => {
    })
    .catch(error => {
      console.error('Error setting remote description:', error);
    });
}


const xhr = new XMLHttpRequest();
const configuration1 = {
  iceServers: [
    { urls: "stun:stun.l.google.com:19302" },
    {
      urls: 'turn:openrelay.metered.ca:80',
      username: 'openrelayproject',
      credential: 'openrelayproject'
    }
  ]
};
const nm1 = document.getElementsByTagName("header")[0].textContent;
xhr.open("POST", "http://localhost:8080", true);
xhr.setRequestHeader("Content-type", "text/plain");
xhr.send("Name: " + nm1);
console.log("Starting " + nm1);

const peerConnection = new RTCPeerConnection(configuration);
xhr.onreadystatechange = function() {
    console.log("reaching here in the code");
    if (xhr.readyState === 4 && xhr.status === 200) {
      console.log("Processing");
      processResponse(xhr.responseText);
      longpollingfunc(xhr);
    }
  };

// Add an event listener for the connection state change
peerConnection.onconnectionstatechange = event => {
    const connectionState = peerConnection.connectionState;
    if (connectionState === 'connected') {
      // Connection is successfully established
      console.log('Connection established!');
    }
  };
function longpollingfunc(xhrr) {
    // Set up the long polling request
    // We want to send a null request to the server
    // Then we wait for a response
    // After the response, we process the response and act on it
    // Then we send a request again to start the process all over again

    // The plan is to receive different responses from the server
    // Act based on these responses
    // Make changes to the HTML page

    var s = document.getElementById("message").value;
    console.log("making another request");
    if(s != ''){
        var nm = document.getElementsByTagName("header")[0].textContent;
        xhrr.open("POST", "http://localhost:8080", true);
        xhrr.setRequestHeader("Content-type", "text/plain");
        xhrr.send("Message: "+nm+": "+s);
        document.getElementById("message").value = "";
    }else{
        var nm = document.getElementsByTagName("header")[0].textContent;
        console.log("making a null request");
        xhrr.open("POST", "http://localhost:8080", true);
        xhrr.setRequestHeader("Content-type", "text/plain");
        xhrr.send("null: "+nm);
    }
    xhrr.onreadystatechange = function() {
        if (xhrr.readyState === 4 && xhrr.status === 200) {
            console.log(xhrr.responseText);
            processResponse(xhrr.responseText);
            longpollingfunc(xhrr);
        }
    };
}

function processResponse(response) {
    console.log("The response is: "+response)
    var nm = document.getElementsByTagName("header")[0].textContent;
    var sidePanel = document.getElementById("targetDiv1");
    var res = response.split(" ");
    switch (res[0]) {
      case "List:":
        for (var i = 2; i < res.length; i++) {
          res[1] = res[1].concat(" ").concat(res[i]);
        }
        console.log(res[1]);
        var resl = res[1].split("&");
        var rest = resl[0].split(",");
        for(var i=0; i<rest.length; i++){
          var newDiv = document.createElement("div");
          newDiv.className = "t-item";
          newDiv.textContent = rest[i];
          sidePanel.appendChild(newDiv);
          console.log(newDiv);
        }
        if(resl[1] != null){
          var resu = resl[1].split(" ");
          var result;
          if(resu[0] != "MList:"){
            for (var i = 1; i < resu.length; i++) {
              result = resu[0].concat(" ").concat(resu[i]);
            }
          }else{
            for (var i = 2; i < resu.length; i++) {
              result = resu[1].concat(" ").concat(resu[i]);
            }
          }
          
          if(result != null){
            var resul = result.split(",");
            for(var i=0; i<resul.length; i++){
            var trg = document.getElementById("chat");
            var newDiv = document.createElement("p");
            newDiv.textContent = resul[i];
            trg.appendChild(newDiv);
            }
          }
        }
        break;
      case "Update:":
        for (var i = 2; i < res.length; i++) {
          res[1] = res[1].concat(" ").concat(res[i]);
        }
        console.log(res[1]);
        console.log(nm);
        if(res[1].trim() != nm.trim()){  
          var newDiv = document.createElement("div");
          newDiv.className = "t-item";
          newDiv.textContent = res[1];
          sidePanel.appendChild(newDiv);
        }
        break;
      case "Message:":
          var trg = document.getElementById("chat");
          for (var i = 2; i < res.length; i++) {
            res[1] = res[1].concat(" ").concat(res[i]);
          }
          var newDiv = document.createElement("p");
          newDiv.textContent = res[1];
          trg.appendChild(newDiv);
          break;
      case "Offer:":
            for (var i = 2; i < res.length; i++) {
                res[1] = res[1].concat(" ").concat(res[i]);
            }
          //create an answer with the peer connection and send it to them
            handleSdpOffer(res[1]);
          break;
      case "Answer:":
          handleSdpAnswer(res[1]);
          break;
      case "ICE:":
            handleIceCandidate(res[1]);
         break;
    }
  }
  
// Function to handle the SDP offer from the streaming client
function handleSdpOffer(offer) {
    // // Create a peer connection object
    console.log(offer);
    const divide = offer.split(":");
    console.log(divide[0]);
    var ofDetails = " ";
    for (let i = 1; i < divide.length; i++) {
        if(i != (divide.length -1 )){
            ofDetails += divide[i] + ":";
        }else{
            ofDetails += divide[i];
        }
    }
    console.log("The offer details are: " + ofDetails.trim());

  
  
  
    // Set the remote description
    peerConnection.setRemoteDescription(new RTCSessionDescription(JSON.parse(ofDetails)))
      .then(() => {
        // Create an SDP answer
        return peerConnection.createAnswer();
      })
      .then(answer => {
        // Set the local description and send the answer to the streaming client
        return peerConnection.setLocalDescription(answer);
      })
      .then(() => {
        // Send the SDP answer to the streaming client via signaling server
        const sdpAnswer = JSON.stringify(peerConnection.localDescription);
        xhr.open('POST', 'http://localhost:8080', true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.send("Answer: "+divide[0]+": "+sdpAnswer);

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                console.log(xhr.responseText);
            }
          };
      })
      .catch(error => {
        console.error('Error creating or sending answer:', error);
      });
  }
  function sendIceToServer(description){
    xhr.open("POST", "http://localhost:8080", true);
    xhr.setRequestHeader("Content-type", "text/plain");
    xhr.send("ICE: "+nm+": " + JSON.stringify(description));

    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4 && xhr.status === 200) {
        console.log("Processing");
        console.log(xhr.responseText);
        longpollingfunc(xhr);
      }
    };
  }

  
 // Function to handle ICE candidates from the streaming client
function handleIceCandidate(candidate) {
    // Add the remote ICE candidate to the peer connection
    // const peerConnection = new RTCPeerConnection();
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate))
    .then(() => {
      console.log("Checking the ice candidates...");
        peerConnection.onicecandidate = event => {
            console.log("ice candidate event started");
            if (event.candidate) {
            // Send the ICE candidate to the remote peer via signaling server
            console.log(event.candidate);
            sendIceToServer(event.candidate);
            }
        };
    })
      .catch(error => {
        console.error('Error adding ICE candidate:', error);
      });
  }
  
  // Function to handle the SDP answer from the streaming client
  function handleSdpAnswer(answer) {
    // Set the remote description
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer))
      .catch(error => {
        console.error('Error setting remote description:', error);
      });
  }
  // Clear the flag on page unload to prevent re-initiating request on subsequent loads
//   window.onbeforeunload = function() {
//     sessionStorage.removeItem("initiateRequest");
//   };
  