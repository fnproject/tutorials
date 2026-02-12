package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"time"

	fdk "github.com/fnproject/fdk-go"
)

func main() {
	fdk.Handle(fdk.HandlerFunc(myHandler))
}

type Person struct {
	Name string `json:"name"`
}

func myHandler(ctx context.Context, in io.Reader, out io.Writer) {
	p := &Person{Name: "World"}
	json.NewDecoder(in).Decode(p)
	msg := struct {
		Msg string `json:"message"`
	}{
		Msg: fmt.Sprintf("Hello %s", p.Name),
	}
	log.Print("Inside Go Hello World function")

	fmt.Println("Before sleep for 500 ms")
	time.Sleep(time.Duration(500) * time.Millisecond)
	fmt.Println("After sleep for 500 ms")
	json.NewEncoder(out).Encode(&msg)
}
