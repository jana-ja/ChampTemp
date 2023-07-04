# ChampTemp

This happened because of a birthday present.

I was growing Champignon mushrooms in my kitchen storage room, which prefer specific temperature and humidity levels.
So I decided to use my birthday present - ESP32, DHT11 sensor, breadboard and some jumper cables - to monitor those values, because why not.

## Circuit

So first I tried to set up the circuit which immediateley turned into a problem when I noticed I needed a 10KΩ pull-up resistor, that had unfortunately not been a part of my present.
I thought who on earth has some spare resistors lying around at their home, but it turned out that I, in fact, was that kind of person.

The next problem was to identify the right resistor (10K) in the package I found, because only some were labeled. 
Looking at a color code chart that also came in the package I noticed I first had to identify which side of the resistors was the right and which the left, because soem pattern were valid read from both sides.
Apparently there is no easy and clear indication which is the left and which the right side, except for the distance between the inner color bands and the edge, that should be bigger on the right side (which wasn't too obvious with mine).
But I also guessed that a 100KΩ resistor might be more common than a 110Ω one and gave it a try (spoiler: it worked).

<p align="center">
  <img src="https://github.com/jana-ja/ChampTemp/assets/38402829/927cbd40-1a42-4a5c-bff0-dba3ba5e5b84" alt="Circuit" width="50%"/>
</p>

## Sketch

- arduino ide
- big nuts

## App

- app fun
<p align="center">
  <img src="https://github.com/jana-ja/ChampTemp/assets/38402829/98555503-7f93-4b04-9af1-80e88eaadb8a" alt="Circuit" width="30%"/>
  <img src="https://github.com/jana-ja/ChampTemp/assets/38402829/e341a13e-d055-40c7-b7b6-ade21947deb0" alt="Circuit" width="30%"/>
  <img src="https://github.com/jana-ja/ChampTemp/assets/38402829/1907e5dc-2355-4f6c-85da-d3e2cedf3959" alt="Circuit" width="30%"/>
</p>
