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
    if (data[0] == 0x3f)
      robot.keyTap("left");
    else
      robot.keyTap("space");
    conn.port.read((err, data) => { readCallback(conn, err, data) });
  }
}

function findServiceChannel(address, serviceName) {
  var channel = null;

  // Assumes that listPairedDevices is sync :\
  deviceinq.listPairedDevices(devices => { 
    for (var i = 0; i < devices.length; i++) {
      var device = devices[i];
      if (device.address == address) {
        console.log(device);
        for (i = 0; i < device.services.length; i++) {
          var service = device.services[i];
          if (service.name == serviceName) {
            channel = service.channel;
            return;
          }
        }
      }
    }
  });

  return channel;
}

// My devices <3
U20i = '8c-64-22-50-5f-44';
LG = '00-a0-c6-65-00-7e';

function connect(address) {

  var channel;

  deviceinq.findSerialPortChannel(address, function(channel){
    
    console.log('Found RFCOMM channel for serial port on %s: ', address, channel);
 
    function try_connect(retries) {
    
      channel = findServiceChannel(address, "Corwin of Amber");

      console.log("===");

      if (!channel) {
        console.error("Service not found.");
        return;
      }

      bluetooth.connect(address, channel, (err, conn) => {
        if (err) {
          console.log("Connection failed; ", err);
          if (retries > 0) {
            console.log("retry...");
            setTimeout(() => { try_connect(retries - 1); }, 1000);
          }
        }
        else
        if (conn) {
          console.log("Connected;", conn);
          conn.port.read((err, data) => { readCallback(conn, err, data) });
        /*
         conn.write(new Buffer("2345"), (err, result) => { 
         console.log("wrote", err, result) 
         });
         */
        }
      });
    }

    //sleep.sleep(2);
    try_connect(3);

  });
}

connect(U20i);
