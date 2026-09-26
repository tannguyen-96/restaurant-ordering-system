import React from 'react'
import { Container, Row, Col } from 'react-bootstrap'
import { useApp } from '../context/AppContext'

export default function Footer() {
  const { siteName } = useApp()

  return (
    <Container fluid as="footer" className="py-2">
      <Row>
        <Col>
            <span className="text-left font-small text-muted">&copy; {new Date().getFullYear()} {siteName}</span>
        </Col>
      </Row>
    </Container>
  )
}
