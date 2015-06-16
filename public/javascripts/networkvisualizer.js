/**
 * 
 */
var toVisData = function(interactions){
	var instances = [];
	var users = [];
	var edges = [];
	var network = null;
	var addedUsers = {};
	var addedTweets = {};
	var c = 1;
	for(var id in interactions){
		  if (interactions.hasOwnProperty(id)) {
	            var i = interactions[id];
	            var user = i.creator;
	            if(!addedUsers.hasOwnProperty(user)){
		            instances.push({id: user, label: user, value: i.popularity, shape: 'image', image: '/assets/images/user.png'});
		            addedUsers[user] = "";
	            }
	            instances.push({id: id, shape: 'circle', label:'', title: i.message, color: toColor(i.class)});
	            addedTweets[id] = "";
	            for(var retweeter in i.retweetedBy)
	            {
	            	if(retweeter > 50)
	            		break;
            		retweeter = i.retweetedBy[retweeter];
	            	if(!addedUsers.hasOwnProperty(retweeter.name)){
	 		            instances.push({id: retweeter.name, label: retweeter.name, shape: 'image', image: '/assets/images/user.png', value: retweeter.popularity});
	 		           addedUsers[retweeter.name] = "";
	            	}
	            	edges.push({from: id, to: retweeter.name, color: toColor(i.class) });
	            }
	            for(var reply in i.replies)
	            {
	            	reply = i.replies[reply];
		            if(!addedTweets.hasOwnProperty(reply.id)){
		            	instances.push({id: reply.id, shape: 'circle', label:'', title: reply.message, color: toColor(reply.class)});
			            addedTweets[reply.id] = "";
		            }
		            if(!addedUsers.hasOwnProperty(reply.creator)){
	 		            instances.push({id: reply.creator, label: reply.creator, shape: 'image', image: '/assets/images/user.png', value: reply.popularity});
	 		            addedUsers[reply.creator] = "";
	            	}
	            	edges.push({from: reply.id, to: id, color: toColor(reply.class), dashes: true });
		            edges.push({from: reply.creator, to: reply.id, color: toColor(reply.class) });

	           	}
	         
	            edges.push({from: user, to: id, color: toColor(i.class) });
	            
	        }
	}
	console.dir(instances);
	console.dir(edges);

	var container = document.getElementById('network');
	var data = {
		nodes: instances,
		edges: edges
	};
	var options = {width: '100%', height: '500px', interaction: {hideEdgesOnDrag: true},
		 edges: { arrows: 'to', smooth: {
		          type: 'discrete' }
			}
			};
	network = new vis.Network(container, data, options);
};


var toColor = function(stringClass){
	switch(stringClass){
	case 'neutral': return '#848484';
	break;
	case 'positiv': return '#5CB85C';
	break;
	case 'negativ': return '#CA0606';
	break;
	default: return '#848484';
	}
};

/*
function draw() {
  // create people
  nodes = [
    {id: 1,  label: 'Algie',   image: DIR + 'Smiley-Angry-icon.png', shape: 'image'},
    {id: 2,  label: 'Alston',  image: DIR + 'Smiley-Grin-icon.png', shape: 'image'},
    {id: 3,  label: 'Barney',  image: DIR + 'User-Administrator-Blue-icon.png', shape: 'image'},
    {id: 4,  label: 'Coley',   image: DIR + 'User-Administrator-Green-icon.png', shape: 'image'},
    {id: 5,  label: 'Grant',   image: DIR + 'User-Coat-Blue-icon.png', shape: 'image'},
    {id: 6,  label: 'Langdon', image: DIR + 'User-Coat-Green-icon.png', shape: 'image'},
    {id: 7,  label: 'Lee',     image: DIR + 'User-Coat-Red-icon.png', shape: 'image'},
    {id: 8,  label: 'Merlin',  image: DIR + 'User-Executive-Green-icon.png', shape: 'image'},
    {id: 9,  label: 'Mick',    image: DIR + 'User-Preppy-Blue-icon.png', shape: 'image'},
    {id: 10, label: 'Tod',     image: DIR + 'User-Preppy-Red-icon.png', shape: 'image'}
  ];

  // create connections
  var color = '#BFBFBF';
  edges = [
    {from: 2,   to: 8,  value: 3,   label: 3,   color: color},
    {from: 2,   to: 9,  value: 5,   label: 5,   color: color},
    {from: 2,   to: 10, value: 1,   label: 1,   color: color},
    {from: 4,   to: 6,  value: 8,   label: 8,   color: color},
    {from: 5,   to: 7,  value: 2,   label: 2,   color: color},
    {from: 4,   to: 5,  value: 1,   label: 1,   color: color},
    {from: 9,   to: 10, value: 2,   label: 2,   color: color},
    {from: 2,   to: 3,  value: 6,   label: 6,   color: color},
    {from: 3,   to: 9,  value: 4,   label: 4,   color: color},
    {from: 5,   to: 3,  value: 1,   label: 1,   color: color},
    {from: 2,   to: 7,  value: 4,   label: 4,   color: color}
  ];

  // create a network
  var container = document.getElementById('mynetwork');
  var data = {
    nodes: nodes,
    edges: edges
  };
  var options = {};
  network = new vis.Network(container, data, options);
}
*/