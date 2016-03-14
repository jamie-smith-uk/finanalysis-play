
var DataStore = Reflux.createStore({

  init: function () {
    this.data = {
      labels: [],
      series: []
    };
    this.monthData = [];
    this.catLabels = [];
    this.trigger();
  },

   getLabels: function(statementData) {
        var foundlabels = [];
        for(var labelIndex in statementData["categories"])
        {
            foundlabels.push(statementData["categories"][labelIndex])
        }
        return foundlabels;
    },

   extractDataForMonth: function(monthData) {
        var data = [];
        for (var categoryKey in monthData["categories"]) {
            data.push(monthData["categories"][categoryKey]["amount"]);
        }
        return data;
    },

    getMonthName: function(monthData) {
        return monthData["month"]
    },


    getDataSeries: function(statementData) {
       var data = [];
       for(var monthIndex in statementData["analysis"]){
           var month = this.getMonthName(statementData["analysis"][monthIndex])
           data[month] = {name:month, data: this.extractDataForMonth(statementData["analysis"][monthIndex])};
       }
       return data;
   },

   copyArray: function(arrayToCopy) {
        var newArray = []
        var count = 0;
        for(var key in arrayToCopy){
           newArray[count] = arrayToCopy[key];
           count++;
        }
        return newArray;
   },

  updateData: function (newData) {
  console.log(newData);
    var series = this.copyArray(this.getDataSeries(newData));
    var labels = this.getLabels(newData);
    this.data = {labels, series};
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
        DataStore.updateData(data);
        DataStore.print();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

  render: function() {
   return (
    <div>
        {this.props.name}

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



React.render(<MyComponent name="Finalysis"/>, document.getElementById('example'));