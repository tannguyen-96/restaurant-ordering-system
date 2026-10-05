import { Container, Row, Col } from 'react-bootstrap'

const Footer = () => {
  return (
    <Container fluid as="footer" className="py-2">
      <Row>
        <Col>
            <span className="text-left font-small text-muted">&copy; {new Date().getFullYear()}</span>
        </Col>
      </Row>
    </Container>
  )
}

export default Footer;