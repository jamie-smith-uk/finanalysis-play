
var DataStore = Reflux.createStore({
  init: function () {
    this.data = {
      labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
      series: [
        [5, 4, 3, 7, 5, 10, 3, 4, 8, 10, 6, 8],
        [3, 2, 9, 5, 4, 6, 4, 6, 7, 8, 7, 4]
      ]
    };

    this.trigger();
  },

  // Change data - for example purposes only
  updateData: function (newData) {


   var pairs = [];
   var labels = [];
   var series = [];
   var series1 = [];
   var thedata = [];
   for(var key in newData){
        pairs.push(<p>{key}: {newData[key]}</p>);
        labels.push(key);
        series1.push(newData[key]);
   }
   series.push(series1);
   thedata = {labels, series};
   this.print();
   this.data = thedata;
   this.print()
   this.trigger();
  },



  print: function() {
  console.log("DataStore:");
  console.log(this.data);
  }


});

var MyComponent = React.createClass({

    getInitialState: function() {
        return { books: [] };
    },

  componentDidMount: function() {
    $.ajax({
      url: "http://localhost:9000/statement",
      dataType: 'json',
      success: function(data) {
        DataStore.print();
        console.log("statement received:");
        console.log(data);
        DataStore.updateData(data);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

  render: function() {
   return (
    <div>
        Hello {this.props.name},
       <Statement data={this.state.books}/>
   </div>)
   }
})


var Statement = React.createClass({
 mixins: [
    Reflux.connect(DataStore,'datastore')
  ],
    render : function() {
       return (
             <ReactChart data={DataStore.data} />

       );
    }
})




var ReactChart = React.createClass({
  componentDidMount: function () {
    this.updateChart(this.props.data);
  },
  componentWillReceiveProps: function (newProps) {
      this.updateChart(newProps.data);
  },
  updateChart: function (data) {
    return new Chartist.Bar('.chart', data);
  },
  render: function () {
    return (
      <div className="chart"></div>
    );
  }
});

var App = React.createClass({
  render: function () {
    return (
      <ReactChart data={somedata} />
    );
  }
});



React.render(<MyComponent name="you!"/>, document.getElementById('example'));
//React.render(<App />, document.querySelector('.app'));