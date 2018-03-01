package main

import (
  "fmt"
  "time"
)

func main() {
  fmt.Println("Before sleep for 500 ms")
  time.Sleep(time.Duration(500) * time.Millisecond)
  fmt.Println("After sleep for 500 ms")
}
