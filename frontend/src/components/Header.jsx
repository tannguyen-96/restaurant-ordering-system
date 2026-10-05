import { Navbar, Container, Nav } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

const Header = () => {
  const { auth } = useAuth();
  return (
    <Navbar expand="lg" className="bg-body-secondary">
      <Container fluid>
        <Navbar.Brand as={Link} to="/">Restaurant Ordering System</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        {!auth?.user && <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <span className="border"></span>
            <Nav.Link as={Link} to="/">Home</Nav.Link>
            <span className="border"></span>
            <Nav.Link className="justify-content-end" as={Link} to="/login">Login</Nav.Link>
          </Nav>
        </Navbar.Collapse>}
        {
          auth?.user?.role === 'admin' && (
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav>
                <span className="border"></span>
                <Nav.Link as={Link} to="/branch">Branch</Nav.Link>
                <span className="border"></span>
                <Nav.Link as={Link} to="/restaurant-table">Restaurant Table</Nav.Link>
                <span className="border"></span>
                <Nav.Link onClick={auth.logout}>Logout</Nav.Link>
              </Nav>
              <Nav className="ms-auto">
                <span className="border"></span>
                <Nav.Link disabled>User:{auth?.user?.name}</Nav.Link>
              </Nav>
            </Navbar.Collapse>
          )
        }
        {
          auth?.user?.role === 'guest' && (
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav>
                <span className="border"></span>
                <Nav.Link as={Link} to="/branch">Menu</Nav.Link>
                <span className="border"></span>
                <Nav.Link as={Link} to="/restaurant-table">Order</Nav.Link>
              </Nav>
              <Nav className="ms-auto">
                <span className="border"></span>
                <Nav.Link disabled>User:{auth?.user?.name + auth?.user?.session}</Nav.Link>
              </Nav>
            </Navbar.Collapse>
          )
        }
      </Container>
    </Navbar>
  )
}

export default Header;
