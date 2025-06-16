console.log(document.referrer);
if (window.location.pathname === '/index.html') {
  // Run specific script for page1.html
  console.log('This is index.html');
  
  console.log("removing lecScriptLoader");
  sessionStorage.setItem('lecScriptLoaded', false);
}else if(window.location.pathname === '/class.html'){
  const data = sessionStorage.getItem("scriptLoaded");
  console.log("This is class.html");
  console.log("data value is ",data);
  
  
  if (document.referrer === `${window.location.protocol}//${window.location.host}/stu-index.html` || data === "false"){
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

    var sidePanel = document.getElementById("targetDiv1");

    //1)Get the name 
    const nm = document.getElementById("name").textContent;
    const className = document.getElementById("classTitle").textContent;



    //set the id after recieving it
    let id ;


    let socket;
    
    let editorTabQueue = {};
    let audioId = null;
    let videoId = null ;
    let isAIdSet = false;
    let isVIdSet = false;

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
          let jsonObject = {name: nm, className: className, title: "Student"};
          socket.send(JSON.stringify(jsonObject));
          
      };
      
      socket.onmessage = (event) => {
          console.log("Message received:", event.data);
          processResponse(event.data);
      };
      
      socket.onclose = (event) => {
          console.log("WebSocket connection closed:", event);
          endTheCall();
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
                      sidePanel.appendChild(newDiv);
                      console.log(newDiv);
                    }
                  }
                  
                  await startRemoteStream();
                  break;
                case "newUser":
      
                  while(result.length > 0){
                    var resi = result.shift();
                    var resi1 = resi.split("%");
                    console.log("new user arrived");
                    var newDiv = document.createElement("div");
                      newDiv.className = "t-item";
                      newDiv.textContent = resi1[0];
                      sidePanel.appendChild(newDiv);
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
                    let details = {id:res.id, title:res.title, name:res.name, type: res.type, sdp: res.sdp};
                    await handleOffer(details);
                  }else if(res[key] == "candidate"){
                    console.log("Recieved a candidate message");
                    let details = {type: res.type, candidate: res.candidate, sdpMid: res.sdpMid, sdpMLineIndex: res.sdpMLineIndex, id: res.id};
                    await handleIce(details);
                  }else if(res[key] == "answer"){
                    let answerDetails = {sender:res.id, type: 'answer', sdp: res.sdp};
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
                case "streamPerson":
                  streamPerson(res[key]);
                  break;
                case "ready":
                  await createPeerConnection(res.id, res[key]);
                  break;
                case "closeStream":
                  if(remoteVideo.srcObject != null){
                    remoteVideo.srcObject = null;
                  }
                  break;
                case "removeUser":
                  handleUserRemoval(res[key]);
                  break;
                case "imgsrc":
                  handleImageFile(res[key]);
                  break;
                case "backTheVideo":
                  handleBackVideo();
                  break;
                case "endTheClass":
                  console.log("Ending the class");
                  endTheCall();
                  break;
                case "visitLink":
                  console.log("Visiting a link");
                  showFrame(res[key]);
                  break;
                case "closeLinkFrame":
                  console.log("closing link frame");
                  closeModalP();
                  break;
                case "scrollFrame":
                  console.log("scrolling frame");
                  scrollFrame(res[key]);
                  break;
                case "editResourceFrame":
                  console.log("editing resource frame");
                  showEditResourceFrame();
                  break;
                case "editorText":
                  console.log("editor text received");
                  showEditorText(res[key]);
                  break;
                case "userEditPerm":
                  console.log(res[key], "is editing");
                  handleEditorPerm(res[key]);
                  break;
                case "createNewTabMenu":
                  console.log("creating a new tab");
                  createNewTabMenu();
                  break;
                case "activeTabName":
                  console.log("writing the tab name");
                  changeTabName(res[key]);
                  break;
                case "changeToTab":
                  console.log("switching to tab");
                  changeToTheTab(res[key]);
                  break;
                case "closeTab":
                  console.log("closing this tab", res[key]);
                  closeTabIndex(res[key]);
                  break;
                case "getFileContent":
                  console.log("getting file content of file", res[key]);
                  createNewTabMenuForResource(res[key], res.content);
                  break;
                case "giveHistory":
                  console.log("getting the history");
                  showTheHistory(res.tabObject, res.activeT);
                  break;
              }
          }
        }
            
      }

function showTheHistory(tObj, actT){
  showEditResourceFrame();
  var rOb = JSON.parse(tObj);

  // Get and sort the keys to process them in order
  var keys = Object.keys(rOb).sort((a, b) => parseInt(a) - parseInt(b));

  // Iterate through sorted keys
  keys.forEach((key, index) => {
    let tabData = rOb[key];

    // Iterate through each key in the nested object
    for (let tabName in tabData) {
      if (tabData.hasOwnProperty(tabName)) {
        let tabContent = tabData[tabName];

        // Log the tab name (key) and its content
        console.log("Tab Name:", tabName, "Content:", tabContent);

        // For the first tab, use changeTabName and showEditorText directly
        if (index == 0) {
          changeTabName(tabName);
          showEditorText(tabContent);
        } else {
          // For subsequent tabs, create a new tab first
          createNewTabMenu();
          changeTabName(tabName);
          showEditorText(tabContent);
        }
      }
    }
  });

  changeToTheTab(actT);
}

function closeTabIndex(closeIndex){
  const modal = document.querySelector(".modal-panel");
  const tabMenu = modal.querySelector(".tab-menu");
  const tabItems = tabMenu.querySelectorAll(".tab-item");
  var editor = document.getElementById("editor");


  if (tabItems.length > 1) {
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
      if(tabItems.length === 2){
        console.log("The close index when length is equal to 2", closeIndex);

        tabItems[closeIndex-1].classList.add("active");
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

      // Clear the queue as it's the last tab
      editorTabQueue = {};
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
    tabItem.textContent = filename;
    editor.innerHTML = escapeHtmlTags(content);
    wrapTags("tag");
    updateEditorQueue();
    updateLineNumbers();
    
    // Insert the new element before the found element
    tabMenu.appendChild(tabItem);
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
function changeToTheTab(tabNo){
  const modal = document.querySelector(".modal-panel");
  const tabMenu = modal.querySelector(".tab-menu");

  // Remove the 'active' class from all tab items
  const tabItems = tabMenu.querySelectorAll(".tab-item");
  tabItems.forEach(item => item.classList.remove("active"));

  // Add the 'active' class to the clicked tab item
  tabItems[tabNo].classList.add("active");

  // Find the position of the active tab
  // const activeIndex = Array.from(tabItems).indexOf(tabItem);
  // console.log('Active tab index:', activeIndex);

  var editor = document.getElementById("editor");

  // Load the content from the queue
  if (editorTabQueue.hasOwnProperty(tabNo)) {
      editor.innerHTML = editorTabQueue[tabNo];
  } else {
      editor.innerHTML = '';
  }
}
function changeTabName(name){
  const modal = document.querySelector(".modal-panel");
  if(modal && modal.querySelector(".tab-menu")){
    const tabMenu = modal.querySelector(".tab-menu");
    const activeTab = tabMenu.querySelector(".active");
    activeTab.textContent = name.trim();
  }
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
    tabItem.textContent = "undefined";
    
    // Insert the new element before the found element
    tabMenu.appendChild(tabItem);
    editor.innerHTML = "";
  }
}
function handleEditorPerm(name){
  const editor = document.getElementById("editor");
  if(name.trim() == nm){
    editor.setAttribute("contenteditable", "true");
    runEditorSettings();
  }else if(editor.hasAttribute("contenteditable")){
    editor.removeAttribute("contenteditable");
  }
}
function runEditorSettings(){
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
              socket.send(JSON.stringify({id: id, editorText: editor.innerHTML}));
          }, 0);
      }else if (event.key === 'Tab') {
          event.preventDefault();
          document.execCommand('insertHTML', false, '&#009;');
          socket.send(JSON.stringify({id: id, editorText: editor.innerHTML}));
      }
  });

  
  var saveFile = document.getElementById("saveEditFile");
  saveFile.addEventListener("click", saveTheFile);

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

    }

}
function showEditorText(text){
  var editor = document.getElementById("editor");
  editor.innerHTML = text;
  const lineNumbers = document.getElementById('line-numbers');

  function updateLineNumbers() {
      const lines = editor.innerHTML.split('<br>').length;
      let lineNumberHTML = '';
      for (let i = 1; i <= lines; i++) {
          lineNumberHTML += `${i}<br>`;
      }
      lineNumbers.innerHTML = lineNumberHTML;
  }
  updateLineNumbers();

  //for the tabs save the content in a queue
  const modal = document.querySelector(".modal-panel");
  if(modal.querySelector(".tab-menu")){
    const tabMenu = modal.querySelector(".tab-menu");
    if(tabMenu.querySelector(".active")){
      const tabItems = tabMenu.querySelectorAll(".tab-item");
      const activTab = tabMenu.querySelector(".active");

      // Find the position of the active tab
      const activeIndex = Array.from(tabItems).indexOf(activTab);
      editorTabQueue[activeIndex] = text;
  } else {
      editorTabQueue[0] = text;
    } 
  }
}
  function showEditResourceFrame() {
    console.log("i got here");
    const modal = document.querySelector(".modal-panel");

    //create the tab menu
    const tabMenu = document.createElement("div");
    tabMenu.className = "tab-menu";
    const tabItem = document.createElement("div");
    // Add multiple classes to the element
    tabItem.classList.add("tab-item", "active");
    tabItem.textContent = "undefined";
    tabMenu.appendChild(tabItem);


    // Create the editor-container div
    const editorContainer = document.createElement("div");
    editorContainer.id = "editor-container";

    // Create the line-numbers div
    const lineNumbers = document.createElement("div");
    lineNumbers.id = "line-numbers";

    // Create the editor div
    const editor = document.createElement("div");
    editor.id = "editor";
    // editor.setAttribute("contenteditable", "true");

    // Append line-numbers and editor to editor-container
    editorContainer.appendChild(lineNumbers);
    editorContainer.appendChild(editor);

    // Append tab-menu and editor-container to modal
    modal.appendChild(tabMenu);
    modal.appendChild(editorContainer);
    modal.style.display = "flex";
}
  
function scrollFrame(scrollValue){
  var scrollV = scrollValue.split(";");
  var frame = document.getElementById("visitedLinkFrame");
  const iframeDocument = frame.contentDocument ;
            
  iframeDocument.documentElement.scrollTop = scrollV[0];
  iframeDocument.documentElement.scrollLeft = scrollV[1];
  // iframeDocument.body.scrollTop = scrollV[0];
  // iframeDocument.body.scrollLeft = scrollV[1];
}
function showFrame(link){
  if(link.trim() != null && link.trim() != ""){
    const modal = document.querySelector(".modal-panel");
    let addr = document.createElement("iframe");
    addr.id = "visitedLinkFrame";
    addr.setAttribute("scrolling", "no");
    let tab = document.createElement("div");
    tab.className = "tab";
    let tabTitle = document.createElement("p");
    tabTitle.className = "tabTitle";
    tabTitle.textContent = link;
    tab.appendChild(tabTitle);

    addr.src = link;
    addr.style.height = "100%";
    modal.appendChild(tab);
    modal.appendChild(addr);
    modal.style.display = "flex";
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
    }else if(modal && document.getElementById("editor-container")){
      var eContainer = document.getElementById("editor-container");
      var tabMenu = document.querySelector(".tab-menu");
      modal.removeChild(tabMenu);
      modal.removeChild(eContainer);
    }else{
        console.log("Iframe does not exist in the modal.");
    }
    modal.style.display = "none";
  }
function endTheCall(){
  window.location.href =  `${window.location.protocol}//${window.location.host}/stu-index.html`;
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

function handleImageFile(imgsrc){
  const viewPort = document.getElementById("preview");
  const vid = document.getElementById("video");
  vid.setAttribute("id", "reVideo1");
  let vid1 = document.getElementById("reVideo1");
  const maxImg = document.createElement("img");
  maxImg.src = imgsrc;
  maxImg.setAttribute("id", "maxImg");
  viewPort.appendChild(maxImg);
}

function handleBackVideo(){
  const viewPort = document.getElementById("preview");
  const vid = document.getElementById("maxImg");
  viewPort.removeChild(vid);
  document.getElementById("reVideo1").setAttribute("id", "video");
}

    const videoHost = document.getElementById("preview");
    const lVideo = document.createElement("video");
    lVideo.setAttribute("id", "video");
    videoHost.appendChild(lVideo);

    const localVideo = document.getElementById('video');

    let iceCandidateQueue = [];
    let isOfferSet = false;
    let rtcQueue = [];

    const box = document.getElementById("preview-control");
    // Create "Start" button
    const start = document.createElement("button");
    start.setAttribute("id", "startButton");
    start.title = "Start";  // Use '=' to set the title property
    start.textContent = "Start";  // Use '=' to set the text content
    // box.appendChild(start);

    const connecting = document.createElement("img");
    connecting.setAttribute("src", "Spinner-1s-200px.gif");
    connecting.setAttribute("alt", "Connecting");
    connecting.setAttribute("id", "connecting");

    let remoteStream; 

    const rVideo = document.createElement("video");
    rVideo.setAttribute("id", "reVideo");
    videoHost.appendChild(rVideo);

    var remoteVideo = document.getElementById("reVideo");

    async function startRemoteStream(){
      console.log("Starting remote stream...");
      let rStream = await navigator.mediaDevices.getUserMedia({ audio: {echoCancellation: true}, video: true });
      remoteStream = rStream;
      socket.send(JSON.stringify({id:id, ready:nm}));
    }
        
    // const startButton = document.getElementById('startButton');
    // startButton.onclick = async () => {
    //     videoHost.appendChild(connecting);
    //     localVideo.style.display = "none";
    //     connecting.style.display = "block";
    //     await startLecMediaStreaming();
    //     // socket.send(JSON.stringify({id: id, ready: id, name:nm}));
    // }
    // startButton.disabled = true;
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
      remoteStream.getTracks().forEach(track => {
        console.log("The track id for student is ", track.id);
        pc.addTrack(track, remoteStream);
      });
      console.log("adding new peer to queue");
      rtcQueue.push(newPeerJson);
      await makeCall(sender, pc);
    }

    async function makeCall(sender, pc) {
        const offer = await pc.createOffer();
        console.log("Sending Offer..."+"Offer: "+JSON.stringify({type: offer.type, sdp: offer.sdp}));
        socket.send(JSON.stringify({id: id, sender: sender, type: offer.type, sdp: offer.sdp , title: "student", name: nm}));
        await pc.setLocalDescription(offer);
      }

    async function handleOffer(offer){
      console.log("this is the offer before processing ", offer);
      let pc = new RTCPeerConnection(configuration);
      
      let iceCandidateQueue = [];
      var newPeerJson = {id: offer.id, title:offer.title, name:offer.name, peer: pc, iceQueue: iceCandidateQueue, isAnswerSet: false};
      await iceListener(pc, offer.id);
      console.log("Got offer.");
      isOfferSet = true;

      pc.ontrack = (e) => {
        console.log("ontrack event:", e);
      
        newPeerJson.stream = e.streams[0];
        console.log("Received Stream:", newPeerJson.stream);
        if(newPeerJson.title === "lecturer"){
          localVideo.srcObject = newPeerJson.stream;
          localVideo.setAttribute("preload", "auto");
        
        
          // Wait for the canplay event before attempting to play
        
          localVideo.oncanplay = () => {
        
            // Create "Play" button
            const player = document.createElement("button");
            player.setAttribute("id", "startButton");
            player.title = "Play";
            player.textContent = "Play";
            box.appendChild(player);
          
            player.onclick = async () => { 
              try {
                await localVideo.play();
                console.log("Video playback started successfully");
                box.removeChild(player);

              } catch (error) {
                console.error("Error starting video playback:", error);
              }
            }
          }
        }

        newPeerJson.stream.getTracks().forEach((track) => {
          console.log("Track kind:", track.kind);
        }); 
      }
      
      console.log("adding new peer to queue");
      rtcQueue.push(newPeerJson);

      // Add tracks to the peer connection
      remoteStream.getTracks().forEach(track => {
        console.log("Adding track:", track.kind);
        pc.addTrack(track, remoteStream);
      });
        
      console.log("setting remote description for ", newPeerJson.name);
        newPeerJson.peer.setRemoteDescription({type:offer.type, sdp:offer.sdp});
        const answer = await pc.createAnswer();
        console.log("Sending Answer: "+JSON.stringify({type: answer.type, sdp: answer.sdp}));
        socket.send(JSON.stringify({id: id, type: 'answer', sdp: answer.sdp, sender: newPeerJson.id}));
        await pc.setLocalDescription(answer);
    }
    async function iceListener(pc, sender){
      console.log("before the ice starts listening for candidates"); 
        pc.onicecandidate = e => {
          console.log("getting ice candidate");
          // Check if e.candidate is not null
          if (e.candidate) {
            const message = {
                id: id,
                sender:sender,
                type: 'candidate',
                candidate: e.candidate.candidate,
                sdpMid: e.candidate.sdpMid,
                sdpMLineIndex: e.candidate.sdpMLineIndex,
            };
      
            console.log("got ice candidate: " + JSON.stringify(message));
            socket.send(JSON.stringify(message));
          }
        };
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
      for (const jsonObject of rtcQueue) {
          console.log("the name is ", jsonObject.name);
          if (ice.id === jsonObject.id) {
              console.log("Setting ice for " + jsonObject.name);
              const pc = jsonObject.peer;
              const iceCandidateQueue = jsonObject.iceQueue;
              const candidate = new RTCIceCandidate({
                  candidate: ice.candidate,
                  sdpMid: ice.sdpMid,
                  sdpMLineIndex: ice.sdpMLineIndex,
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

      document.addEventListener("DOMContentLoaded", function () {
        document.getElementById("message").addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                setMessage();
            }
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
    });
    
    document.getElementById("chatBtn").addEventListener( "click" ,setMessage);

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

    async function startLecMediaStreaming(){
      console.log("Starting lecturer stream");
      for (var i = 0; i < rtcQueue.length; i++){
        let jsonObject = rtcQueue[i];
        console.log("The stream we are start is for the title ", jsonObject.title);
        if(jsonObject.title === "lecturer"){
          //add the stream to lecturer stream and set the label
          console.log("getting the stream of lecturer");
            
          
          break;
        }
      }
    }

    function streamPerson(person) {
      console.log("starting remote stream for ", person);
      
      if (person === nm) {
          const videoTrack = remoteStream.getVideoTracks()[0];
          if (videoTrack) {
              const videoStream = new MediaStream([videoTrack]);
              remoteVideo.srcObject = videoStream;
              remoteVideo.play().then(() => {
                  console.log("Video playback started successfully for local user");
              }).catch(error => {
                  console.error("Error starting video playback for local user:", error);
              });
          } else {
              console.error("No video tracks found in remoteStream for local user");
          }
      } else {
          for (let i = 0; i < rtcQueue.length; i++) {
              const jsonObject = rtcQueue[i];
              if (jsonObject.name === person) {
                  console.log("The stream we are starting is for the name ", jsonObject.name);
  
                  if (jsonObject.stream) {
                    console.log("Have a stream for this student");
                      remoteVideo.srcObject = jsonObject.stream;
                      remoteVideo.setAttribute("preload", "auto");
  
                      remoteVideo.oncanplay = () => {
                          // Create "Play" button
                          const player = document.createElement("button");
                          player.setAttribute("id", "startButton");
                          player.title = "Play";
                          player.textContent = "Play";
                          box.appendChild(player);
  
                          player.onclick = async () => {
                              try {
                                  await remoteVideo.play();
                                  console.log("Video playback started successfully for ", jsonObject.name);
                                  box.removeChild(player);
                              } catch (error) {
                                  console.error("Error starting video playback for ", jsonObject.name, ":", error);
                              }
                          };
                      };
                  } else {
                      console.error("No stream found for ", jsonObject.name);
                  }
                  break;
              }
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
    sessionStorage.setItem('scriptLoaded', true);
  }
}