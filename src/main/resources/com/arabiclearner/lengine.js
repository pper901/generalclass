console.log(document.referrer);
if (window.location.pathname === '/index.html') {
  // Run specific script for page1.html
  console.log('This is index.html');
  
  console.log("removing lecScriptLoader");
  sessionStorage.setItem('lecScriptLoaded', false);
}else if(window.location.pathname === '/class.html'){
  const data = sessionStorage.getItem("lecScriptLoaded");
  console.log("This is class.html");
  console.log("data value is ",data);
  
  
  if (document.referrer === `${window.location.protocol}//${window.location.host}/index.html` || document.referrer === `${window.location.protocol}//${window.location.host}/` || data === "false"){
        /*
      *  Copyright (c) 2021 The WebRTC project authors. All Rights Reserved.
      *
      *  Use of this source code is governed by a BSD-style license
      *  that can be found in the LICENSE file in the root of the source
      *  tree.
      *  When the lecturer login
      *  1)Get the name
      *  2)Send the name and the title to the server with json.stringify
      *  3)The server responds with a unique id; save the id
      * 
      *  2)start peer connection and create offer
      *  3)create a json object 
      *  4)add the id and the offer created to the json object
      *  5)send the stringify json object to the server
      *  6)get the response
      */
  
      'use strict';
  
  
      const configuration = {};
  
      (async() => {
        const response = await fetch("https://arabiclearner.metered.live/api/v1/turn/credentials?apiKey=db0b8ad2e7a4c7813ee941e161f145745d06");
        const iceServers = await response.json();
        configuration.iceServers = iceServers
      })();
  
  
  
      const prot = window.location.protocol;
      const host = window.location.hostname;
      console.log(prot);
  
      let socket;
  
      var sidePanel = document.getElementById("targetDiv1");
  
      //1)Get the name 
      const nm = document.getElementById("name").textContent;
      const className = document.getElementById("classTitle").textContent;
  
      //define the queue to store each rtcconnection
      let rtcQueue = [];
  
      //set the id after recieving it
      let id;
      let clientQueue = [];
  
      if(prot == "https:"){
        console.log("using secure sever");
        socket = new WebSocket("wss://"+host+":8443/websocket");
      }else{
        console.log("using less secure sever");
        socket = new WebSocket("ws://"+host+":8081/websocket");
      }
  
  
      socket.onopen = (event) => {
          console.log("WebSocket connection opened:", event);
          // You can send a message here if needed
          //2)send the name and title to the server
          let jsonObject = {name: nm, className: className, title: "Lecturer"};
          socket.send(JSON.stringify(jsonObject));
      };
  
      socket.onmessage = (event) => {
          console.log("Message received:", event.data);
          processResponse(event.data);
      };
  
      socket.onclose = (event) => {
          console.log("WebSocket connection closed:", event);
      };
  
      async function processResponse(response){
        console.log(response);
  
        const res = JSON.parse(response);
        console.log("The object length is "+Object.keys(res).length);
        console.log(res);
        for (let key in res) {
          let result = extractValuesByKey(response, key);
          console.log("The result i got from extraction is: "+result);
          if (res.hasOwnProperty(key)) {
              switch(key){
                case "id":
                  if(id == null){
                    id = res[key];
                    var newDiv = document.createElement("div");
                    newDiv.className = "t-item";
                    newDiv.textContent = nm;
                    sidePanel.appendChild(newDiv);
                  }
                  break;
                case "noMessage":
                    break;
                case "list":
                  
                  while(result.length > 0){
                    var resi = result.shift();
                    var rest = resi.split(",");
                    for(var i=0; i<rest.length; i++){
                      var newDiv = document.createElement("div");
                      newDiv.className = "t-item";
                      newDiv.textContent = rest[i];
                      newDiv.onclick = handleUserPeer;
                      sidePanel.appendChild(newDiv);
                      console.log(newDiv);
                    }
                  }
                  break;
                case "newUser":
  
                  while(result.length > 0){
                    var resi = result.shift();
                    var resi1 = resi.split("%");
                    console.log("new user arrived");
                    var newDiv = document.createElement("div");
                      newDiv.className = "t-item";
                      newDiv.textContent = resi1[0];
                      newDiv.onclick = handleUserPeer;
                      sidePanel.appendChild(newDiv);

                      //send the current history state();
                      sendCurrentScreenState(resi1[1], resi1[0]);
                      // await createPeerConnection(resi1[1], resi1[0]);
                  }
                    break;
                case "chatMessage":
                  while(result.length > 0){
                    var resi = result.shift();
                    handleChat(resi);
                  }
                  break;
                case "chatlist":
                  while(result.length > 0){
                    var resi = result.shift(); 
                    var rest = resi.split(",");
                    for(var i=0; i<rest.length; i++){
                      handleChat(rest[i]);
                    }
                  }
                  break;
                case "type":
                  if(res[key] == "offer"){
                    let details = {type: res.type, sdp: res.sdp};
                    await handleOffer(details);
                  }else if(res[key] == "candidate"){
                    let details = {type: res.type, candidate: res.candidate, sdpMid: res.sdpMid, sdpMLineIndex: res.sdpMLineIndex, id: res.id};
                    await handleIce(details);
                  }else if(res[key] == "answer"){
                    let answerDetails = {type: 'answer', sdp: res.sdp, sender: res.id};
                    await handleAnswer(answerDetails);
                  }
                  break;
                case "icelist":
                    var rest = res[key];
                    while (rest.length > 0) {
                      console.log("handling ice from ice list"+ JSON.stringify(rest[0]));
                        await handleIce(rest[0]);
                        rest.shift();
                    }
                  break;
                case "ready":
                  if(!startButton.disabled){
                    clientQueue.push({userId: res.id, userName: res[key]});
                  }else{
                    await createPeerConnection(res.id, res[key]);
                  }
                  break;
                case "removeUser":
                  handleUserRemoval(res[key]);
                  break;
                case "filetype":
                  console.log("recieved file type", res[key]);
                  console.log("with the source being ", res.filesrc);
                  break;
                case "editorText":
                  console.log("editor text received");
                  showEditorText(res[key]);
                  break;
                case "getAllFileResource": 
                  console.log("getting all the file resources");
                  showAllFileResources(res[key]);
                  break;
                case "getFileContent":
                  console.log("getting file content of file", res[key]);
                  createNewTabMenuForResource(res[key], res.content);
                  break;
              }
          }
        }
      
      }
      function sendCurrentScreenState(uid, name) {
        console.log("sending current state");
        const modal = document.querySelector(".modal-panel");
        
        if (modal && modal.querySelector(".tab-menu")) {
          const tabMenu = modal.querySelector(".tab-menu");
          const tabItems = tabMenu.querySelectorAll(".tab-item");
      
          let tOb = {}; // Declare tOb as an object
          tabItems.forEach(item => {
            let tOb1 = {};
            var tabText = item.querySelector(".tabText").textContent;
            var indexOfTab = Array.from(tabItems).indexOf(item);
            var tabContent = editorTabQueue[indexOfTab];
            tOb1[tabText] = tabContent; // Use tabText as the key and tabContent as the value
            tOb[indexOfTab] = tOb1;
          });
      
          var activeTab = tabMenu.querySelector(".active");
          var indexOfAcTab = Array.from(tabItems).indexOf(activeTab);
          socket.send(JSON.stringify({
            id: id, 
            giveHistory: uid, 
            tabObject: JSON.stringify(tOb), 
            activeT: JSON.stringify(indexOfAcTab)
          }));
        }
        if(modal && document.getElementById("userEditPerm")){
          const selectCont = document.getElementById("userEditPerm");
          let opt = document.createElement("option");
          opt.value = name;
          opt.textContent = name;
          selectCont.appendChild(opt);

        }
      }
      
function createNewTabMenuForResource(filename, content){
  const modal = document.querySelector(".modal-panel");
  var editor = document.getElementById("editor");
  
  if(modal && modal.querySelector(".tab-menu")){
    const tabMenu = modal.querySelector(".tab-menu");
    const tabItems = tabMenu.querySelectorAll(".tab-item");
    tabItems.forEach(item => item.classList.remove("active"));
    const tabItem = document.createElement("div");
    // Add multiple classes to the element
    tabItem.classList.add("tab-item", "active");
    var tabText = document.createElement("p");
    tabText.textContent = filename;
    tabText.className = "tabText";

    var closeTab = document.createElement("p");
    closeTab.textContent = "x";
    closeTab.className = "closeTab";
    closeTab.addEventListener("click", function() {
      const allClTb = tabMenu.querySelectorAll(".closeTab");
      const tabItems = tabMenu.querySelectorAll(".tab-item");
  
      // Find the position of the close button clicked
      const closeIndex = Array.from(allClTb).indexOf(this);
  
      if (allClTb.length > 1) {
          // Remove the tab from the queue
          delete editorTabQueue[closeIndex];
  
          // Reassign the keys to be sequential
          const newQueue = {};
          let newIndex = 0;
  
          for (let i = 0; i < tabItems.length; i++) {
              if (i !== closeIndex) {
                  newQueue[newIndex] = editorTabQueue[i];
                  newIndex++;
              }
          }
  
          editorTabQueue = newQueue;
  
  
          // Remove the tab element
          tabMenu.removeChild(tabItems[closeIndex]);
          console.log("The close index when length is greater than 1", closeIndex);
          if(allClTb.length === 2){
            console.log("The close index when length is equal to 2", closeIndex);

            tabItems[closeIndex-1].classList.add("active");
            document.getElementById('filename').value = tabItems[closeIndex-1].querySelector(".tabText").textContent;

            // Update the editor content
            if (editorTabQueue.hasOwnProperty(closeIndex-1)) {
                editor.innerHTML = editorTabQueue[closeIndex-1];
            } else {
                editor.innerHTML = '';
            }
          }else{
            
            // Update the editor content
            if (editorTabQueue.hasOwnProperty(closeIndex)) {
              editor.innerHTML = editorTabQueue[closeIndex];
            } else {
                editor.innerHTML = '';
            }
          }
      } else {
          // If this is the last tab
          tabText = "undefined";
          editor.innerHTML = '';
          tabItems[0].classList.add("active");
          document.getElementById('filename').value = tabItems[0].querySelector(".tabText").textContent;
  
          // Clear the queue as it's the last tab
          editorTabQueue = {};
        }
        socket.send(JSON.stringify({id: id, closeTab: closeIndex}));
    });
  
    tabItem.appendChild(tabText);
    tabItem.appendChild(closeTab);

    var filen = document.getElementById('filename');
    filen.value = filename;
    editor.innerHTML = escapeHtmlTags(content);
    wrapTags("tag");
    updateEditorQueue();
    updateLineNumbers();

    tabText.addEventListener('click', function() {
      // Remove the 'active' class from all tab items
      const tabItems = tabMenu.querySelectorAll(".tab-item");
      tabItems.forEach(item => item.classList.remove("active"));

      // Add the 'active' class to the clicked tab item
      tabItem.classList.add("active");

      // Find the position of the active tab
      const activeIndex = Array.from(tabItems).indexOf(tabItem);
      console.log('Active tab index:', activeIndex);
      filen.value = tabItem.querySelector(".tabText").textContent;

      //send the tab i am using to the student
      socket.send(JSON.stringify({id: id, changeToTab: activeIndex}));

      // Load the content from the queue
      if (editorTabQueue.hasOwnProperty(activeIndex)) {
          console.log(editorTabQueue[activeIndex]);
          editor.innerHTML = editorTabQueue[activeIndex];
      } else {
          editor.innerHTML = '';
      }
    });

    // Find the element you want to insert before
    const createNewTab = tabMenu.querySelector(".createNewFile");
    
    // Insert the new element before the found element
    tabMenu.insertBefore(tabItem, createNewTab);

    //send to others
    socket.send(JSON.stringify({id: id, createNewTabMenu: "newTab"}));
  }
}



function updateLineNumbers() {
    const lineNumbers = document.getElementById('line-numbers');
    var editor = document.getElementById("editor");
    const lines = editor.innerHTML.split('<br>').length;
    let lineNumberHTML = '';
    for (let i = 1; i <= lines; i++) {
        lineNumberHTML += `${i}<br>`;
    }
    lineNumbers.innerHTML = lineNumberHTML;
}
function wrapTags(tag) {
  console.log("i got here");
  var editor = document.getElementById("editor");
  const content = editor.innerHTML
  console.log(content);
  
  const regex = /(&lt;[^&]*&gt;|<[^>]*>)/g;
  var openingTags = false;

  const wrappedContent = content.replace(regex, function(match) {
      // Regular expression to check if the match is already wrapped
      const spanRegex = new RegExp(`<span class="${tag}">`, 'g');
      const closeSpanRegex = new RegExp(`</span>`,'g');

      // Check if the match is already wrapped
      if (spanRegex.test(match)) {
          openingTags = true;
          return ""; // Return the match unaltered if it's already wrapped or a <br><br>
      }else if(closeSpanRegex.test(match)){
          if(openingTags){
              return "";
          }
      }else if(match === '<br>'){
          return match;
      }
  
      // Otherwise, wrap the match with the span tag
      return `<span class="${tag}">${match}</span>`;

  });
  console.log(wrappedContent);
  editor.innerHTML = wrappedContent;
  // updateLineNumbers();
}
function escapeHtmlTags(htmlString) {
  // First replace '><' with '> <br><br> &#009; <'
  let processedString = htmlString.replace(/</g, '&lt;').replace(/>/g, '&gt;');
  
  // Then escape '<' and '>'
  processedString = processedString.replace(/&gt;&lt;/g, '&gt; <br> &#009; &lt;');
  console.log(processedString);
  
  return processedString;
}
function updateEditorQueue(){
  const modal = document.querySelector(".modal-panel");
  //for the tabs save the content in a queue
  if(modal.querySelector(".tab-menu")){
    const tabMenu = modal.querySelector(".tab-menu");
    if(tabMenu.querySelector(".active")){
      const tabItems = tabMenu.querySelectorAll(".tab-item");
      const activTab = tabMenu.querySelector(".active");

      // Find the position of the active tab
      const activeIndex = Array.from(tabItems).indexOf(activTab);
      editorTabQueue[activeIndex] = editor.innerHTML;
  } else {
      editorTabQueue[0] = editor.innerHTML;
    } 
  }
}
function showAllFileResources(files) {
  console.log("clicked on the resource library");
  if (event.target === modal && modal.contains(resModal)) {
    console.log()
      modal.removeChild(resModal);
  }
  let allFiles = files.split(";");
  let resModal = document.createElement("div");
  resModal.className = "res-modal";
  let modal = document.querySelector(".modal-panel");

  allFiles.forEach(file => {
      var holdFile = document.createElement("p");
      holdFile.className = "res-name";
      holdFile.textContent = file;
      holdFile.addEventListener("click", function(event) {
          event.stopPropagation();
          let fileArr = holdFile.textContent.split(".");
          if (fileArr.length === 1) {
              // this is a folder, so open the folder
              socket.send(JSON.stringify({ id: id, getAllFileResource: fileArr[0] }));
          } else {
              let webDevExt = ['html', 'js', 'css', 'txt'];
              if (webDevExt.includes(fileArr[1])) {
                  socket.send(JSON.stringify({ id: id, getFileContent: holdFile.textContent.trim() }));
              } else {
                  alert("unsupported file format");
              }
          }
          modal.removeChild(resModal);
      });
  });

  modal.appendChild(resModal);

  modal.addEventListener("click", function(event) {
      if (event.target === modal && modal.contains(resModal)) {
        console.log()
          modal.removeChild(resModal);
      }
  });
  resModal.addE
}


  function showEditorText(text){
    var editor = document.getElementById("editor");
    editor.innerHTML = text;
  }

  function handleChat(message) {
    let result = message.split("&^");
    const chat = document.getElementById("chat");
    let messageCont = document.createElement("div");
    messageCont.setAttribute("id", "chatMessage");

    let messageUser = document.createElement("p");
    messageUser.setAttribute("id", "chatUser");
    messageUser.textContent = result[0];

    let messageText = document.createElement("p");
    messageText.setAttribute("id", "chatText");
    messageText.textContent = result[1].replace(/\\\c/g, ",");

    messageCont.appendChild(messageUser);
    messageCont.appendChild(messageText);
    chat.appendChild(messageCont);

    let nMAlert = document.createElement("p");
    nMAlert.setAttribute("id", "newMAlert");
    nMAlert.textContent = "New Message";
    nMAlert.setAttribute("class", "fadeInOut");

    nMAlert.onclick = function() {
        // Automatically scroll to the latest message
        chat.scrollTop = chat.scrollHeight;
        cont.removeChild(nMAlert);
    };

    let cont = document.getElementById("t-panel");

    // Check if the new message alert already exists before adding it
    if (!document.getElementById("newMAlert")) {
        cont.appendChild(nMAlert);
    }

    function removeAlert() {
        // Check if the new message is still in the viewable area
        const chatScrollHeight = chat.scrollHeight;
        const chatScrollTop = chat.scrollTop;
        const chatClientHeight = chat.clientHeight;

        if (chatScrollTop + chatClientHeight >= chatScrollHeight) {
            // If the new message hasn't been scrolled past, remove the alert
            cont.removeChild(nMAlert);
        }

        // Remove the event listener to avoid multiple calls
        cont.removeEventListener("mouseenter", removeAlert);
    }

    // Add the event listener to check when the mouse enters the chat box
    cont.addEventListener("mouseenter", removeAlert);
}


  
  function handleUserRemoval(id){
    let username = null;
    for(let i = 0; i < rtcQueue.length; i++){
      let jsonObject = rtcQueue[i];
      if(jsonObject.id === id){
        username = jsonObject.name;
        break;
      }
    }
    if(username !== null){
      for(let i = 0; i < sidePanel.childNodes.length; i++){
        const child = sidePanel.childNodes[i];
        if(child.nodeType === 1 && child.textContent === username){
          sidePanel.removeChild(child);
          break;
        }
      }
    }
  }

      async function sendFile() {
        console.log("uploading file");
        try {
          const fileInput = document.getElementById("fileInput");
          const file = fileInput.files[0];
          
          const reader = new FileReader();
          reader.onload = async function(event) {
            const fileData = event.target.result.split(',')[1]; // Extract Base64 data
            
            try {
              // Send the Base64 data along with file type and name to the server
              const response = await fetch(`${window.location.protocol}//${window.location.host}/upload`, {
                method: 'POST',
                body: JSON.stringify({
                  fileType: file.type,
                  fileName: file.name,
                  data: fileData
                }),
                headers: {
                  'Content-Type': 'application/json'
                }
              });
      
              // Handle the response from the server as needed
              if (response.ok) {
                const responseData = await response.text();
                console.log('File uploaded successfully with response', responseData);
                const jRes = JSON.parse(responseData);
                // Regular expression pattern to match image file types
                var imageTypePattern = /^image\/.*/;
      
                switch (jRes.filetype) {
                  case "image/png":
                    showImage(jRes.file);
                    break;
                  default:
                    // Check if the file type matches the image type pattern
                    if (imageTypePattern.test(jRes.filetype)) {
                      // If it's an image file, show it
                      showImage(jRes.file);
                    } else {
                      // Handle other file types
                      console.log("Unsupported file type: " + jRes.filetype);
                    }
                    break;
                }
              } else {
                console.error('Failed to upload file:', response.statusText);
              }
            } catch (error) {
              console.error('Error uploading file:', error);
            }
          };
          
          // Read the file as binary data
          reader.readAsDataURL(file);
        } catch (error) {
          console.error('Error reading file:', error);
        }
      }
      
  
  
    function showImage(srcAddr){
      const holder = document.getElementById("upload-data");
      const  imgTag = document.createElement("img");
      console.log("creating image element for : ", srcAddr);
      imgTag.src = srcAddr;
      imgTag.setAttribute("id","mini-img");
      imgTag.onclick = handleFile;
      holder.style.height = "49px";
      holder.appendChild(imgTag);
    }
    
    function handleFile(){
      const viewPort = document.getElementById("preview");
      const vid = document.getElementById("video");
      vid.setAttribute("id", "reVideo1");
      let vid1 = document.getElementById("reVideo1");
      let addr = this.src;
      const maxImg = document.createElement("img");
      maxImg.src = addr;
      maxImg.setAttribute("id", "maxImg");
      viewPort.appendChild(maxImg);
      socket.send(JSON.stringify({id: id, imgsrc: addr}));
  }
  
  function handleBackVideo(){
      const viewPort = document.getElementById("preview");
      const vid = document.getElementById("maxImg");
      viewPort.removeChild(vid);
      this.setAttribute("id", "video");
      socket.send(JSON.stringify({id: id, backTheVideo: "lecturer"}));
  }

    function readFileAsDataURL(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (event) => resolve(event.target.result);
        reader.onerror = (error) => reject(error);
        reader.readAsDataURL(file);
      });
    }
    document.getElementById("u-btn").addEventListener("click", sendFile);
  
      
  
      const box = document.getElementById("preview-control");
  
      // Create "Start" button
      const start = document.createElement("button");
      start.setAttribute("id", "startButton");
      start.title = "Start";  // Use '=' to set the title property
      start.textContent = "Start";  // Use '=' to set the text content
      box.appendChild(start);
      
      // Create "Hangup" button
      const hang = document.createElement("button");
      hang.setAttribute("id", "hangupButton");
      hang.title = "Hangup";  // Use '=' to set the title property
      hang.textContent = "Hangup";  // Use '=' to set the text content
      box.appendChild(hang);
        
      
      const startButton = document.getElementById('startButton');
      const hangupButton = document.getElementById('hangupButton');
      hangupButton.disabled = true;
      
      const videoHost = document.getElementById("preview");
      const lVideo = document.createElement("video");
      lVideo.setAttribute("id", "video");
      lVideo.onclick = handleBackVideo;
      videoHost.appendChild(lVideo);
      
      const localVideo = document.getElementById('video');
      
      const rVideo = document.createElement("video");
      rVideo.setAttribute("id", "reVideo");
      videoHost.appendChild(rVideo);
  
      let remoteVideo = document.getElementById("reVideo");
      
      let localStream;
      
      
      startButton.onclick = async () => {
          try {
            let lStream = await navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: true } , video: true });
            localStream = lStream;
            // Create a new MediaStream containing only the video track
            const videoStream = new MediaStream([localStream.getVideoTracks()[0]]);
            localVideo.srcObject = videoStream;
            localVideo.play();
        
            startButton.disabled = true;
            hangupButton.disabled = false;
            if(clientQueue.length > 0){
              for(let i = 0; i < clientQueue.length; i++){
                let jClient = clientQueue[i];
                await createPeerConnection(jClient.userId, jClient.userName);
              }
              clientQueue = [];
            }else{
              console.log("Ready to send data");
            }
      
          } catch (error) {
            console.error("Error accessing media devices:", error);
          }
        };
        
        
        hangupButton.onclick = async () => {
          hangup();
          // signaling.postMessage({type: 'bye'});
        };
        
        async function hangup() {
          for (var i = 0; i < rtcQueue.length; i++){
            var jsonObject = rtcQueue[i];
            let pc = jsonObject.peer;
            if (pc) {
              pc.close();
              pc = null;
            }
          }
          
          localStream.getTracks().forEach(track => track.stop());
          localStream = null;
          startButton.disabled = false;
          hangupButton.disabled = true;
        };
  
      async function createPeerConnection(sender, name) {
          const pc = new RTCPeerConnection(configuration);
          let iceCandidateQueue = [];
  
          //create a new peer for each user and add to the queue
          var newPeerJson = {id: sender, name:name, peer: pc, iceQueue: iceCandidateQueue, isAnswerSet: false};
            console.log("before the ice starts listening for candidates"); 
            pc.onicecandidate = e => {
              console.log("getting ice candidate");
              // Check if e.candidate is not null
              if (e.candidate) {
                const message = {
                    id: id,
                    sender: sender,
                    type: 'candidate',
                    candidate: e.candidate.candidate,
                    sdpMid: e.candidate.sdpMid,
                    sdpMLineIndex: e.candidate.sdpMLineIndex,
                };
          
                console.log("got ice candidate: " + JSON.stringify(message));
                socket.send(JSON.stringify(message));
                
              }
            };
            console.log("getting tracks");
            //so we then add the streams from pc to remoteVideo
          
          
            pc.ontrack = (e) => {
              console.log("ontrack event:", e);
            
              newPeerJson.stream = e.streams[0];
              console.log("Received Stream:", newPeerJson.stream);
  
              newPeerJson.stream.getTracks().forEach((track) => {
                console.log("Track kind:", track.kind);
              }); 
          }
          console.log("adding new peer to queue");
          rtcQueue.push(newPeerJson);
  
          localStream.getTracks().forEach(track => {
            console.log("The track id for lecturer is ", track.id);
            pc.addTrack(track, localStream);
          });
          makeCall(sender, pc);
        }
      async function makeCall(sender, pc) {
            const offer = await pc.createOffer();
            console.log("Sending Offer..."+"Offer: "+JSON.stringify({type: offer.type, sdp: offer.sdp}));
            socket.send(JSON.stringify({id: id, sender: sender, type: offer.type, sdp: offer.sdp , title: "lecturer", name: nm}));
            await pc.setLocalDescription(offer);
          }
  
        async function handleAnswer(answer) {
          console.log('Handling answer...');
          console.log("Recieved Answer: "+JSON.stringify(answer));
          for (var i = 0; i < rtcQueue.length; i++){
          let jsonObject = rtcQueue[i];
          console.log("JsonObject id "+jsonObject.id);
          console.log("answer Id "+answer.sender);
            if(answer.sender === jsonObject.id){
              console.log("Setting answer for "+jsonObject.name);
              let pc = jsonObject.peer;
              await pc.setRemoteDescription(new RTCSessionDescription({type: answer.type, sdp: answer.sdp}));
              jsonObject.isAnswerSet = true;
              console.log('Remote description set successfully.');
              break;
            }
          }
        }
  
        async function handleIce(ice) {
          console.log("Handling this ice " + ice);
          for (var i = 0; i < rtcQueue.length; i++) {
              let jsonObject = rtcQueue[i];
              let pc;
              let iceCandidateQueue;
              if (ice.id === jsonObject.id) {
                  console.log("Setting ice for " + jsonObject.name);
                  pc = jsonObject.peer;
                  iceCandidateQueue = jsonObject.iceQueue;
                  const candidate = new RTCIceCandidate({
                      candidate: ice.candidate,
                      sdpMid: ice.sdpMid,
                      sdpMLineIndex: ice.sdpMLineIndex
                  });
      
                  if (pc.remoteDescription) {
                      console.log("adding ice to RTCConnection");
                      await pc.addIceCandidate(candidate);
                      console.log("added ice successfully");
                  } else {
                      console.log("adding ice to queue");
                      iceCandidateQueue.push(candidate);
                  }
              }
          }
      }
  
      let timer = setInterval(checkOffer,500);
      async function checkOffer(){
        for (var i = 0; i < rtcQueue.length; i++){
          let jsonObject = rtcQueue[i];
          let isAnswerSet = jsonObject.isAnswerSet;
          let iceCandidateQueue = jsonObject.iceQueue;
          if(isAnswerSet){
        
            if (iceCandidateQueue.length !== 0) {
              // Iterate through the array without modifying it during the loop
              for (let i = 0; i < iceCandidateQueue.length; i++) {
                  await handleIce(iceCandidateQueue[i]);
              }
          
              // Clear the array after processing all elements
              iceCandidateQueue.length = 0;
            }
          }
        }
        clearInterval(timer);
      }
  
      async function setMessage() {
        let fmessage = document.getElementById("message").value;
        if (fmessage.trim() !== "") {
            let message = fmessage.replace(/,/g, "\\c");
            const chat = document.getElementById("chat");
            let messageCont = document.createElement("div");
            messageCont.setAttribute("id", "chatMessage");
          
            let messageUser = document.createElement("p");
            messageUser.setAttribute("id", "chatUser");
            messageUser.textContent = nm;
          
            let messageText = document.createElement("p");
            messageText.setAttribute("id", "chatText");
            messageText.textContent = fmessage;
          
            messageCont.appendChild(messageUser);
            messageCont.appendChild(messageText);
            chat.appendChild(messageCont);
          
            // Scroll to the latest message
            chat.scrollTop = chat.scrollHeight;
    
            let jOb = { id: id, chatMessage: nm + "&^ " + message };
            socket.send(JSON.stringify(jOb));
        }
    
        // Clear the input field
        document.getElementById("message").value = "";
    }
    
      let editorTabQueue = {};
        document.addEventListener("DOMContentLoaded", function () {
          document.getElementById("message").addEventListener("keydown", function (event) {
              if (event.key === "Enter") {
                  event.preventDefault();
                  setMessage();
              }
          });
          document.getElementById("chatBtn").addEventListener( "click" ,setMessage);

          
          document.getElementById("endClassBtn").addEventListener("click", function() {
            console.log("ending the class");
            socket.send(JSON.stringify({id: id, endTheClass: "endclass", className: className}));
            // Redirect the user to a new address
            window.location.href =  `${window.location.protocol}//${window.location.host}/index.html`;
          });

          document.getElementById("chat").addEventListener("scroll", function(){
            let cont = document.getElementById("t-panel");
            let nMAlert = document.getElementById("newMAlert");
            if(cont.hasChildNodes(nMAlert)){ 
              let chat = document.getElementById("chat");
              if(chat.scrollTop === chat.scrollHeight){
                cont.removeChild(nMAlert);
              }
            }
          });

          document.getElementById("v-btn").addEventListener("click", showFrame);
          document.getElementById("x-close").addEventListener("click", closeModalP);
          document.getElementById("editResource").addEventListener("click", showEditResourceFrame);
          
      });
      
    async function showEditResourceFrame() {
      console.log("i got here");
      const modal = document.querySelector(".modal-panel");
  
      // Create the edit-menu div
      const editMenu = document.createElement("div");
      editMenu.className = "edit-menu";
  
      // Create the first edit-item div
      const editItem1 = document.createElement("div");
      editItem1.className = "edit-item";
      const label1 = document.createElement("label");
      label1.setAttribute("for", "filename");
      label1.textContent = "Enter Filename: ";
      const input1 = document.createElement("input");
      input1.type = "text";
      input1.id = "filename";
      input1.name = "filename";
      const button1 = document.createElement("button");
      button1.textContent = "Save the file";
      button1.id = "saveEditFile";
      button1.className = "rs-btn";
  
      editItem1.appendChild(label1);
      editItem1.appendChild(input1);
      editItem1.appendChild(button1);
  
      // Create the second edit-item div
      const editItem2 = document.createElement("div");
      editItem2.className = "edit-item";
      const label2 = document.createElement("label");
      label2.setAttribute("for", "userEditPerm");
      label2.textContent = "Select User to Edit File";
      const select = document.createElement("select");
      select.id = "userEditPerm";
      select.className = "rs-btn";
      select.name = "userEditPerm";
      const names = document.querySelectorAll(".t-item");
      const option1 = document.createElement("option");
      option1.textContent = "None";
      select.appendChild(option1);
      for(i=0; i<names.length; i++){
        const options =  document.createElement("option");
        if(names[i].textContent.trim() !== nm){
          options.value = names[i].textContent;
          options.textContent = names[i].textContent;
          select.appendChild(options);
        }
      }
  
      editItem2.appendChild(label2);
      editItem2.appendChild(select);
  
      // Create the third edit-item div
      const editItem3 = document.createElement("div");
      editItem3.className = "edit-item";
      const button2 = document.createElement("button");
      button2.id = "resourceLibrary";
      button2.className = "rs-btn";
      button2.textContent = "Open Resource Library";

      //add an event listener for this button 
      button2.addEventListener("click", function(){
        //get all the files in the home directory
        socket.send(JSON.stringify({id: id, getAllFileResource: "getAll"}));
      });
  
      editItem3.appendChild(button2);
  
      // Append all edit items to edit-menu
      editMenu.appendChild(editItem1);
      editMenu.appendChild(editItem2);
      editMenu.appendChild(editItem3);

      //create the tab menu
      const tabMenu = document.createElement("div");
      tabMenu.className = "tab-menu";
      const tabItem = document.createElement("div");
      // Add multiple classes to the element
      tabItem.classList.add("tab-item", "active");
      var tabText = document.createElement("p");
      tabText.textContent = "undefined";
      tabText.className = "tabText";
  
      var closeTab = document.createElement("p");
      closeTab.textContent = "x";
      closeTab.className = "closeTab";
      closeTab.addEventListener("click", function() {
        const allClTb = tabMenu.querySelectorAll(".closeTab");
        const tabItems = tabMenu.querySelectorAll(".tab-item");
    
        // Find the position of the close button clicked
        const closeIndex = Array.from(allClTb).indexOf(this);
    
        if (allClTb.length > 1) {
            // Remove the tab from the queue
            delete editorTabQueue[closeIndex];
    
            // Reassign the keys to be sequential
            const newQueue = {};
            let newIndex = 0;
    
            for (let i = 0; i < tabItems.length; i++) {
                if (i !== closeIndex) {
                    newQueue[newIndex] = editorTabQueue[i];
                    newIndex++;
                }
            }
    
            editorTabQueue = newQueue;
    
            // Update the editor content
            if (editorTabQueue.hasOwnProperty(closeIndex)) {
                editor.innerHTML = editorTabQueue[closeIndex];
            } else {
                editor.innerHTML = '';
            }
    
            // Remove the tab element
            tabMenu.removeChild(tabItems[closeIndex]);
            console.log("The close index when length is greater than 1", closeIndex);
            if(allClTb.length === 2){
              console.log("The close index when length is equal to 2", closeIndex);
  
              tabItems[closeIndex-1].classList.add("active");
              document.getElementById('filename').value = tabItems[closeIndex-1].querySelector(".tabText").textContent;
  
              // Update the editor content
              if (editorTabQueue.hasOwnProperty(closeIndex-1)) {
                  editor.innerHTML = editorTabQueue[closeIndex-1];
              } else {
                  editor.innerHTML = '';
              }
            }else{
              
              // Update the editor content
              if (editorTabQueue.hasOwnProperty(closeIndex)) {
                editor.innerHTML = editorTabQueue[closeIndex];
              } else {
                  editor.innerHTML = '';
              }
            }
        } else {
            // If this is the last tab
            tabText = "undefined";
            editor.innerHTML = '';
            tabItems[0].classList.add("active");
            document.getElementById('filename').value = tabItems[0].querySelector(".tabText").textContent;
    
            // Clear the queue as it's the last tab
            editorTabQueue = {};
          }
          socket.send(JSON.stringify({id: id, closeTab: closeIndex}));
      });
      tabItem.appendChild(tabText);
      tabItem.appendChild(closeTab);

      const addButton = document.createElement("div");
      addButton.className = "createNewFile";
      addButton.textContent = "+";
      addButton.setAttribute("title", "Create a new file");

      tabText.addEventListener('click', function() {
        // Remove the 'active' class from all tab items
        const tabItems = tabMenu.querySelectorAll(".tab-item");
        tabItems.forEach(item => item.classList.remove("active"));

        // Add the 'active' class to the clicked tab item
        tabItem.classList.add("active");
        document.getElementById('filename').value = tabItem.querySelector(".tabText").textContent;

        // Find the position of the active tab
        const activeIndex = Array.from(tabItems).indexOf(tabItem);
        console.log('Active tab index:', activeIndex);

        //send the tab i am using to the student
        socket.send(JSON.stringify({id: id, changeToTab: activeIndex}));

        // Load the content from the queue
        if (editorTabQueue.hasOwnProperty(activeIndex)) {
            editor.innerHTML = editorTabQueue[activeIndex];
        } else {
            editor.innerHTML = '';
        }
      });

      addButton.addEventListener("click", function(){
        createNewTabMenu();
      });
      tabMenu.appendChild(tabItem);
      tabMenu.appendChild(addButton);


  
      // Create the editor-container div
      const editorContainer = document.createElement("div");
      editorContainer.id = "editor-container";
  
      // Create the line-numbers div
      const lineNumbers = document.createElement("div");
      lineNumbers.id = "line-numbers";
  
      // Create the editor div
      const editor = document.createElement("div");
      editor.id = "editor";
      editor.setAttribute("contenteditable", "true");
  
      // Append line-numbers and editor to editor-container
      editorContainer.appendChild(lineNumbers);
      editorContainer.appendChild(editor);
  
      // Append edit-menu and editor-container to modal
      modal.appendChild(editMenu);
      modal.appendChild(tabMenu);
      modal.appendChild(editorContainer);
      modal.style.display = "flex";
      runEditorSettings();
      
      socket.send(JSON.stringify({id: id, editResourceFrame: "edit"}));
      document.getElementById("userEditPerm").addEventListener("change", function(){
        console.log(document.getElementById("userEditPerm").value, " is editing");
        socket.send(JSON.stringify({id: id, userEditPerm: document.getElementById("userEditPerm").value }));
      });
      document.getElementById('filename').addEventListener("input", function(){
        var filen = document.getElementById('filename').value;
        if(filen.trim() !== null && filen.trim() !== ""){
          if(modal && modal.querySelector(".tab-menu")){
            const tabMenu = modal.querySelector(".tab-menu");
            const activeTab = tabMenu.querySelector(".active");
            var tabText = activeTab.querySelector(".tabText");

            tabText.textContent = filen.trim();

            //send the name to the students
            socket.send(JSON.stringify({id: id, activeTabName: filen.trim()}));
          }
        }
      });
  }

  function createNewTabMenu(){
    const modal = document.querySelector(".modal-panel");
    var editor = document.getElementById("editor");
    
    if(modal && modal.querySelector(".tab-menu")){
      const tabMenu = modal.querySelector(".tab-menu");
      const tabItems = tabMenu.querySelectorAll(".tab-item");
      tabItems.forEach(item => item.classList.remove("active"));
      const tabItem = document.createElement("div");
      // Add multiple classes to the element
      tabItem.classList.add("tab-item", "active");
      
      var tabText = document.createElement("p");
      tabText.textContent = "undefined";
      tabText.className = "tabText";
  
      var closeTab = document.createElement("p");
      closeTab.textContent = "x";
      closeTab.className = "closeTab";
      closeTab.addEventListener("click", function() {
        const allClTb = tabMenu.querySelectorAll(".closeTab");
        const tabItems = tabMenu.querySelectorAll(".tab-item");
    
        // Find the position of the close button clicked
        const closeIndex = Array.from(allClTb).indexOf(this);
    
        if (allClTb.length > 1) {
            // Remove the tab from the queue
            delete editorTabQueue[closeIndex];
    
            // Reassign the keys to be sequential
            const newQueue = {};
            let newIndex = 0;
    
            for (let i = 0; i < tabItems.length; i++) {
                if (i !== closeIndex) {
                    newQueue[newIndex] = editorTabQueue[i];
                    newIndex++;
                }
            }
    
            editorTabQueue = newQueue;
    
            // Update the editor content
            if (editorTabQueue.hasOwnProperty(closeIndex)) {
                editor.innerHTML = editorTabQueue[closeIndex];
            } else {
                editor.innerHTML = '';
            }
    
            // Remove the tab element
            tabMenu.removeChild(tabItems[closeIndex]);
            console.log("The close index when length is greater than 1", closeIndex);
            if(allClTb.length === 2){
              console.log("The close index when length is equal to 2", closeIndex);
  
              tabItems[closeIndex-1].classList.add("active");
              document.getElementById('filename').value = tabItems[closeIndex-1].querySelector(".tabText").textContent;
  
              // Update the editor content
              if (editorTabQueue.hasOwnProperty(closeIndex-1)) {
                  editor.innerHTML = editorTabQueue[closeIndex-1];
              } else {
                  editor.innerHTML = '';
              }
            }else{
              
              // Update the editor content
              if (editorTabQueue.hasOwnProperty(closeIndex)) {
                editor.innerHTML = editorTabQueue[closeIndex];
              } else {
                  editor.innerHTML = '';
              }
            }
        } else {
            // If this is the last tab
            tabText = "undefined";
            editor.innerHTML = '';
            tabItems[0].classList.add("active");
            console.log("The close index when length is less than 1", closeIndex);

            document.getElementById('filename').value = tabItems[0].querySelector(".tabText").textContent;
    
            // Clear the queue as it's the last tab
            editorTabQueue = {};
          }
          socket.send(JSON.stringify({id: id, closeTab: closeIndex}));
      });


      tabItem.appendChild(tabText);
      tabItem.appendChild(closeTab);
      var filen = document.getElementById('filename');
      filen.value = "";
      editor.innerHTML = "";

      tabText.addEventListener('click', function() {
        // Remove the 'active' class from all tab items
        const tabItems = tabMenu.querySelectorAll(".tab-item");
        tabItems.forEach(item => item.classList.remove("active"));

        // Add the 'active' class to the clicked tab item
        tabItem.classList.add("active");

        // Find the position of the active tab
        const activeIndex = Array.from(tabItems).indexOf(tabItem);
        console.log('Active tab index:', activeIndex);
        filen.value = tabItem.querySelector(".tabText").textContent;

        //send the tab i am using to the student
        socket.send(JSON.stringify({id: id, changeToTab: activeIndex}));

        // Load the content from the queue
        if (editorTabQueue.hasOwnProperty(activeIndex)) {
            console.log(editorTabQueue[activeIndex]);
            editor.innerHTML = editorTabQueue[activeIndex];
        } else {
            editor.innerHTML = '';
        }
      });

      // Find the element you want to insert before
      const createNewTab = tabMenu.querySelector(".createNewFile");
      
      // Insert the new element before the found element
      tabMenu.insertBefore(tabItem, createNewTab);

      //send to others
      socket.send(JSON.stringify({id: id, createNewTabMenu: "newTab"}));
    }
  }

  function runEditorSettings(){
    const modal = document.querySelector(".modal-panel");
    const editor = document.getElementById('editor');
    const lineNumbers = document.getElementById('line-numbers');

    function updateLineNumbers() {
        const lines = editor.innerHTML.split('<br>').length;
        let lineNumberHTML = '';
        for (let i = 1; i <= lines; i++) {
            lineNumberHTML += `${i}<br>`;
        }
        lineNumbers.innerHTML = lineNumberHTML;
    }

    function moveCursorToEnd(element) {
        const range = document.createRange();
        const selection = window.getSelection();
        range.selectNodeContents(element);
        range.collapse(false);
        selection.removeAllRanges();
        selection.addRange(range);
        element.focus();
    }
    updateLineNumbers();

    editor.addEventListener('input', function() {
        updateLineNumbers();

        //for the tabs save the content in a queue
        updateEditorQueue();
        socket.send(JSON.stringify({id: id, editorText: editor.innerHTML}));
        // wrapTags('tag');
    });
    editor.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault(); 
            document.execCommand('insertHTML', false, '<br><br>');
            setTimeout(() => {
                wrapTags("tag");
                moveCursorToEnd(editor);
                updateEditorQueue();
                socket.send(JSON.stringify({id: id, editorText: editor.innerHTML}));
            }, 0);
        }else if (event.key === 'Tab') {
            event.preventDefault();
            document.execCommand('insertHTML', false, '&#009;');
            moveCursorToEnd(editor);
            updateEditorQueue();
            socket.send(JSON.stringify({id: id, editorText: editor.innerHTML}));
        }
    });

    
    var saveFile = document.getElementById("saveEditFile");
    saveFile.addEventListener("click", saveTheFile);

    function updateEditorQueue(){
      //for the tabs save the content in a queue
      if(modal.querySelector(".tab-menu")){
        const tabMenu = modal.querySelector(".tab-menu");
        if(tabMenu.querySelector(".active")){
          const tabItems = tabMenu.querySelectorAll(".tab-item");
          const activTab = tabMenu.querySelector(".active");

          // Find the position of the active tab
          const activeIndex = Array.from(tabItems).indexOf(activTab);
          editorTabQueue[activeIndex] = editor.innerHTML;
      } else {
          editorTabQueue[0] = editor.innerHTML;
        } 
      }
    }

    function wrapTags(tag) {
      console.log("i got here");
      const content = editor.innerHTML
      console.log(content);
      
      const regex = /(&lt;[^&]*&gt;|<[^>]*>)/g;
      var openingTags = false;

      const wrappedContent = content.replace(regex, function(match) {
          // Regular expression to check if the match is already wrapped
          const spanRegex = new RegExp(`<span class="${tag}">`, 'g');
          const closeSpanRegex = new RegExp(`</span>`,'g');

          // Check if the match is already wrapped
          if (spanRegex.test(match)) {
              openingTags = true;
              return ""; // Return the match unaltered if it's already wrapped or a <br><br>
          }else if(closeSpanRegex.test(match)){
              if(openingTags){
                  return "";
              }
          }else if(match === '<br>'){
              return match;
          }
      
          // Otherwise, wrap the match with the span tag
          return `<span class="${tag}">${match}</span>`;

      });
      console.log(wrappedContent);
      editor.innerHTML = wrappedContent;
      // updateLineNumbers();
  }

  function saveTheFile() {
      const filename = document.getElementById('filename').value;
      if (!filename) {
          alert('Please enter a filename.');
          return;
      }

      // Get the editor content and remove the span tags
      const editorContent = editor.innerHTML;
      const cleanContent = editorContent.replace(/<span class="tag">(.*?)<\/span>/g, '$1');
      const cleanContentLd = cleanContent.replace(/&lt;/g, '<');
      const cleanContentGd = cleanContentLd.replace(/&gt;/g, '>');
      console.log("The title is", filename);
      console.log("This is the editor content", editorContent);
      console.log("The content is",cleanContent);
      console.log("The final content is ", cleanContentGd);
      socket.send(JSON.stringify({id: id, saveFileData: cleanContentGd, fileName: filename}));

      }

  }
    
      function showFrame() {
        const link = document.getElementById("linkInput").value;
        if (link.trim() !== "") {
            const modal = document.querySelector(".modal-panel");
            let addr = document.createElement("iframe");
            let tab = document.createElement("div");
            tab.className = "tab";
            let tabTitle = document.createElement("p");
            tabTitle.className = "tabTitle";
            tabTitle.textContent = link;
            tab.appendChild(tabTitle);
    
            socket.send(JSON.stringify({id: id, visitLink: link}));
    
            addr.src = link;
            addr.style.height = "100%";
            modal.appendChild(tab);
            modal.appendChild(addr);
            modal.style.display = "flex";
    
           // Assuming addr is your iframe element
            addr.addEventListener('load', () => {
              // Get the iframe document
              const iframeDocument = addr.contentDocument || addr.contentWindow.document;

              // Listen to scroll events within the iframe
              iframeDocument.addEventListener('scroll', () => {
                  // Get the scroll position
                  const scrollTop = iframeDocument.documentElement.scrollTop || iframeDocument.body.scrollTop || iframeDocument.defaultView.pageYOffset;
                  const scrollLeft = iframeDocument.documentElement.scrollLeft || iframeDocument.body.scrollLeft || iframeDocument.defaultView.pageXOffset;

                  // Send scroll position via WebSocket
                  socket.send(JSON.stringify({ id: id, scrolling: `${scrollTop};${scrollLeft}` }));
              });
            });

        }
    }
    

    function closeModalP(){
      var modal = document.querySelector(".modal-panel");
      if (modal && modal.querySelector("iframe")) {
        var frame = modal.querySelector("iframe");
        var tab = modal.querySelector(".tab");
        modal.removeChild(frame);
        modal.removeChild(tab);
      } else if(modal && modal.querySelector(".edit-menu")){
        //remove edit-menu and editor container
        var editMenu = document.querySelector(".edit-menu");
        var tabMenu = document.querySelector(".tab-menu");
        var eContainer = document.getElementById("editor-container");
        modal.removeChild(editMenu);
        modal.removeChild(tabMenu);
        modal.removeChild(eContainer);
      }else{
          console.log("Iframe does not exist in the modal.");
      }
      modal.style.display = "none";
      socket.send(JSON.stringify({id: id, closeLinkFrame: "closeFrame"}));
    }
      
  
  
      function extractValuesByKey(jsonString, key) {
        const valuesArray = [];
        const regex = new RegExp(`"${key}"\\s*:\\s*("(.*?)"|\\d*|true|false|null)`, 'g');
        let match;
  
        while ((match = regex.exec(jsonString)) !== null) {
          const [, value] = match[0].match(/:\s*"(.*)"/) || [, match[0]];
          valuesArray.push(value);
        }
  
        return valuesArray;
      }
  
      function handleUserPeer() {
        var userName = this.textContent; // 'this' refers to the clicked element
        // Now we loop through the queue to find the username 
        console.log("Clicked user: " + userName);
        // Loop through the rtcQueue array
        for (var i = 0; i < rtcQueue.length; i++) {
          var jsonObject = rtcQueue[i];
          console.log("The name clicked is "+jsonObject.name);
  
          // Check if userName matches the name property in the JSON object
          if (userName === jsonObject.name) {
              // Get the rtcPeer of that name
              var rtcPeer = jsonObject.peer;
  
              console.log("Found rtcPeer for user " + userName + ": " + rtcPeer);
  
              //add the stream to lecturer stream and set the label
              
              remoteVideo.srcObject = jsonObject.stream;
              remoteVideo.setAttribute("preload", "auto");
            
            
              // Wait for the canplay event before attempting to play
            
              remoteVideo.oncanplay = () => {
            
                // Create "Play" button
                const player = document.createElement("button");
                player.setAttribute("id", "startButton");
                player.title = "Play";
                player.textContent = "Play";
                box.appendChild(player);
  
                const closeButton = document.createElement("button");
                closeButton.setAttribute("id","closeButton");
                closeButton.title = "close";
                closeButton.textContent = "Close";
              
                player.onclick = async () => { 
                  try {
                    await remoteVideo.play();
                    console.log("Video playback started successfully");
                    box.removeChild(player);
                    box.appendChild(closeButton);
                    closeButton.onclick = async () => {
                      remoteVideo.srcObject = null;
                      box.removeChild(closeButton);
                      socket.send(JSON.stringify({id: id, closeStream: "closeStream"}));
                    }
                    //tell other student to start streaming the same person
                    socket.send(JSON.stringify({id:id, streamPerson: jsonObject.name}));
                      
                  } catch (error) {
                    console.error("Error starting video playback:", error);
                  }
                }
              }
  
              // Break out of the loop since we found a match
              break;
          }
        }
      }
      // Function to send a keep-alive message to the server
      function sendKeepAlive() {
        // Check if the WebSocket connection is open
        if (socket.readyState === WebSocket.OPEN) {
            // Send a keep-alive message (you can define the message content as needed)
            socket.send(JSON.stringify({id: id, keepalive: 'keep-alive'}));
        }
      }

      // Set interval to send keep-alive messages every 30 seconds (adjust as needed)
      setInterval(sendKeepAlive, 30000);
  
      function handleAudioFilter(stream) {
      // Create audio context
      const audioContext = new (window.AudioContext || window.webkitAudioContext)();
  
      // Create media stream source for audio
      const audioSource = audioContext.createMediaStreamSource(stream);
  
      // Create noise cancellation filter for audio
      const noiseCancellation = audioContext.createBiquadFilter();
      noiseCancellation.type = 'lowpass';
      noiseCancellation.frequency.value = 500; // Adjust frequency based on your needs
  
      // Connect the audio nodes
      audioSource.connect(noiseCancellation);
      noiseCancellation.connect(audioContext.destination);
  
      // Create a new MediaStream for the processed audio
      const processedAudioStream = audioContext.createMediaStreamDestination().stream;
  
      // Add the audio track to the processed stream
      processedAudioStream.addTrack(processedAudioStream.getAudioTracks()[0]);
  
      // Add the video track to the processed stream
      const processedStream = new MediaStream([processedAudioStream.getAudioTracks()[0], stream.getVideoTracks()[0]]);
  
      return processedStream;
      }
  
      function handleError(error) {
        console.error('Error accessing microphone:', error);
      }
  
    sessionStorage.setItem('lecScriptLoaded', true);
  }else{
    console.log("found lecScriptLoader");
  }
}
