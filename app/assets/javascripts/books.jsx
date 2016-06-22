
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
        for(var labelIndex in statementData)
        {
            foundlabels.push(statementData[labelIndex]["category"])
        }
        return foundlabels;
    },

   extractDataForMonth: function(monthData) {
        var data = [];
        for (var categoryKey in monthData) {
            data.push(monthData[categoryKey]["amount"]);
        }
        return data;
    },

    getMonthName: function(monthData) {
        return monthData["month"]
    },


    getDataSeries: function(statementData) {
       var data = [];
       for(var monthIndex in statementData){
           var month = "month";
           data[month] = {name:month, data: this.extractDataForMonth(statementData)};
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


var MonthSelector = React.createClass({

  getInitialState:function(){
      return {selectValue:'January'};
  },

  handleChange:function(e){
    this.setState({selectValue:e.target.value});
    console.log("Data Change " + e.target.value)
  },

  render: function() {

    return (
      <div>
      <select
        value={this.state.selectValue}
        onChange={this.handleChange}
      >
       <option value="1">January</option>
        <option value="2">February</option>
      </select>
      </div>
    );
  }
})

var MyComponent = React.createClass({

    getInitialState: function() {
        return { books: [], selectMonthValue:'1', selectYearValue:'2016'};
    },

  componentDidMount: function() {
    this.update(this.state.selectMonthValue, this.state.selectYearValue);
  },

  update: function(month, year) {
      $.ajax({
        url: "http://localhost:9000/monthStatement/" + month + "/" + year,
        dataType: 'json',
        success: function(data) {
          DataStore.updateData(data);
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
  },

  updateCategory: function(category) {
        $.ajax({
          url: "http://localhost:9000/categoryStatement/" + category,
          dataType: 'json',
          success: function(data) {
            DataStore.updateData(data);
          }.bind(this),
          error: function(xhr, status, err) {
            console.error(this.props.url, status, err.toString());
          }.bind(this)
        });
    },

  handleStateUpdate: function() {
    this.update(this.state.selectMonthValue, this.state.selectYearValue);
  },

  handleMonthChange:function(e){
      this.setState({selectMonthValue:e.target.value}, this.handleStateUpdate);
  },

  handleYearChange:function(e){
      this.setState({selectYearValue:e.target.value}, this.handleStateUpdate);
  },

  handleCategoryChange:function(e){
      this.updateCategory(e.target.value);
  },

  render: function() {
   return (
        <div class="container">
        <p>{this.props.name}</p>
        <p>
             <select
                value={this.state.selectMonthValue}
                onChange={this.handleMonthChange}>
                   <option value="1">January</option>
                   <option value="2">February</option>
                   <option value="3">March</option>
                   <option value="4">April</option>
                   <option value="5">May</option>
                   <option value="6">June</option>
                   <option value="7">July</option>
                   <option value="8">August</option>
                   <option value="9">September</option>
                   <option value="10">October</option>
                   <option value="11">November</option>
                   <option value="12">December</option>
             </select>

             <select
                value={this.state.selectYearValue}
                onChange={this.handleYearChange}>
                    <option value="2015">2015</option>
                    <option value="2016">2016</option>
             </select>
        </p>

        <p>
             <select onChange={this.handleCategoryChange}>
                <option value="Travel">Travel</option>
                <option value="Food">Food</option>
                <option value="Bills">Bills</option>
                <option value="Going Out">Going Out</option>
                <option value="General Expense">Expenses</option>
                <option value="Money In">Money In</option>
                <option value="Childcare">Childcare</option>
                <option value="Savings">Savings</option>
                <option value="Standing Order">Standing Order</option>
             </select>
         </p>
         <Statement data={this.state.books}/>
         </div>
         )
   }
})

var lineChartOptions = {};

var Statement = React.createClass({
 mixins: [Reflux.connect(DataStore,'datastore')],

 render : function() {
    return (
          <ReactChart data={DataStore.data} options={lineChartOptions}/>
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