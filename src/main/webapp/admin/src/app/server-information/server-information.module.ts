import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ServerInformationPageRoutingModule } from './server-information-routing.module';

import { ServerInformationPage } from './server-information.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ServerInformationPageRoutingModule
  ],
  declarations: [ServerInformationPage]
})
export class ServerInformationPageModule {}
