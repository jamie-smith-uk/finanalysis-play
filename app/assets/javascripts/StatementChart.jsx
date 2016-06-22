class FirstComponent extends React.Component {

    render () {
        return <div>Hello {this.props.name}</div>
    }
}

React.render(<FirstComponent />, document.getElementById('es6example'));