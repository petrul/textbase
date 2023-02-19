import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RelocationsPageRoutingModule } from './relocations-routing.module';

import { RelocationsPage } from './relocations.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RelocationsPageRoutingModule
  ],
  declarations: [RelocationsPage]
})
export class RelocationsPageModule {}
