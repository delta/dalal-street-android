![CircleCI build status](https://circleci.com/gh/delta/dalal-street-server.png)
![Go Report Card](https://goreportcard.com/badge/github.com/delta/dalal-street-server)

# Dalal-Street
Dalal Street App

Pragyan Event : Manigma : Online

Are you a stock market enthusiast? Do the words Sensex and Nasdaq mean more to you than just names at the bottom of news channels? At Pragyan 2020, we have the perfect platform for you to test your trading skills, and see if you understand the evolution of trade from barter to modern-day stock exchange here at Dalal Street! Grit your teeth, take calculated risks under pressure and become the richest man on the market!

Register now and see if you can become the next Wolf of Dalal Street!

# Setup

Clone this repository
```
git clone https://github.com/delta/dalal-street-android.git
```
Update submodule for protocol buffers:
```
git submodule init
git submodule update --recursive --remote
```

In Android Studio just clean build should work.

Uses GRPC to send and receive requests/responses from web socket API (Dalal server).
