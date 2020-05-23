﻿using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;

namespace PlovdivUniversity_OOP_ASD
{
    class Tasks
    {

        public void CarRace()
        {
            Dictionary<string, Dictionary<string, CurrentCar>> currentRacers = new Dictionary<string, Dictionary<string, CurrentCar>>();

            string enterCarData = Console.ReadLine();
            string[] dataCollectedFromInut;
            while (!enterCarData.Equals("end", StringComparison.OrdinalIgnoreCase))
            {
                dataCollectedFromInut = enterCarData.Split(" ");

            }
        }

        private struct CurrentCar
        {
            private string brand;
            private int horsePower;
            private double weight;
            private int till100Boost;
            private int after100Boost;



            public CurrentCar(params string[] currentCarData) :this()
            {
                
            this.Brand = currentCarData[2];
            this.Weight = double.Parse(currentCarData[3]);
            this.HorsePower = int.Parse(currentCarData[4]);
            this.Till100Boost = this.HorsePower;
            this.After100Boost = this.HorsePower;
            this.ApplyBoost(currentCarData[5]);

                
            }
            private void ValidateBrand(string brand)
            {
                this.Brand = brand;
            }

            public string Brand
            {
                get
                {
                    return this.brand;
                }
               private set
                {
                    this.brand = value;

                }
            }

            public double Weight
            {
                get
                {
                    return this.weight;
                }

                private set
                {
                    if (value <= 0)
                    {
                        throw new ArgumentException("Invali Weight");

                    }
                    else
                    {
                        this.Weight = value;
                    }
                }
            }

            public int HorsePower
            {
                get
                {
                    return this.HorsePower;

                }
                private set
                {
                    if (value <= 0)
                    {
                        throw new ArgumentException("Invalid Horse Powers");

                    }
                }
            }

            public int Till100Boost
            {
                get
                {
                    return this.till100Boost;

                }
                private set
                {
                    this.till100Boost = 1 / (value) * 1000 / 2;
                }
            }


            public int After100Boost
            {
                get
                {
                    return this.till100Boost;

                }
                private set
                {
                    this.after100Boost = 1 / (value) * 1000;
                }
            }

            public void ApplyBoost(string currentBoost)
            {
                switch (currentBoost.ToLower())
                {
                    case "do-100":

                        this.horsePower -= (this.horsePower / 100) * 30;
                        break;
                    case "do-200":
                        this.horsePower -= (this.horsePower / 100) * 20;
                        break;
                }
            }

            public override string ToString()
            {
                return string.Format($"{this.Brand} {this.Weight}-weight {this.Till100Boost}-seconds for 100km/h and {this.after100Boost} for 200");

            }

        }
    }
}
