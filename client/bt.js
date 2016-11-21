const bluetooth = require('node-bluetooth');
const sleep = require('sleep');
const robot = require('robotjs');

const deviceinq = new bluetooth.DeviceINQ();
var pairedDevices = [];

deviceinq.listPairedDevices(devices => { 
  for (var i = 0; i < devices.length; i++) {
    var dv = devices[i];
    console.log(dv); 
  } 
  pairedDevices = devices;
  });


deviceinq.on('found', function(address, name){
 
  console.log('Found: ' + address + ' with name ' + name);
 
  deviceinq.findSerialPortChannel(address, function(channel){
    
    console.log('Found RFCOMM channel for serial port on %s: ', name, channel);
 
  });
  
});
 
deviceinq.on('finished', function(){
  
  console.log('scan finished.');
  
});
 
//deviceinq.inquire();

function readCallback(conn, err, data) {
  if (err) {
    console.log("error; ", err);
  }
  else {
    console.log(data);
    robot.keyTap("space");
    conn.port.read((err, data) => { readCallback(conn, err, data) });
  }
}

// My devices <3
U20i = '8c-64-22-50-5f-44';
LG = '00-a0-c6-65-00-7e';

function connect(address) {

  var channel;

  deviceinq.findSerialPortChannel(address, function(channel){
    
    console.log('Found RFCOMM channel for serial port on %s: ', address, channel);
 
    sleep.sleep(2); // annoying; better way to sync?
    
deviceinq.listPairedDevices(devices => { 
  for (var i = 0; i < devices.length; i++) {
    var dv = devices[i];
    console.log(dv);
  } 
  pairedDevices = devices;
  });

  console.log("===");

  for (var i = 0; i < pairedDevices.length; i++) {
    var device = pairedDevices[i];
    if (device.address == address) {
      for (i = 0; i < device.services.length; i++) {
        var service = device.services[i];
        if (service.name == "Corwin of Amber") {
          channel = service.channel;
        }
      }
    }
  }

  if (!channel) {
    console.error("Service not found.");
    return;
  }

//console.log(pairedDevices);

  bluetooth.connect(address, channel, (err, conn) => {
  console.log("Connected;", err, conn);
  if (conn) {
    conn.port.read((err, data) => { readCallback(conn, err, data) });
    /*
    conn.write(new Buffer("2345"), (err, result) => { 
     console.log("wrote", err, result) 
    });
    */
  }
  });

  });
}

connect(U20i);
